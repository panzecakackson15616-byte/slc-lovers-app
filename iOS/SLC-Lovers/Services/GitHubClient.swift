import Foundation

/// GitHub Contents API 客户端
/// 仅负责与 GitHub 通信，不关心加密/业务
actor GitHubClient {

    private let baseURL = "https://api.github.com"
    private let token: String
    private let owner: String
    private let repo: String

    init(token: String, owner: String, repo: String) {
        self.token = token
        self.owner = owner
        self.repo = repo
    }

    // MARK: - 仓库检查

    /// 检查仓库是否存在
    func checkRepoExists() async throws -> Bool {
        let url = URL(string: "\(baseURL)/repos/\(owner)/\(repo)")!
        let (data, response) = try await performRequest(url: url, method: "GET")
        guard let http = response as? HTTPURLResponse else { return false }
        return http.statusCode == 200
    }

    // MARK: - 文件读取

    struct GitHubFile: Codable {
        let content: String        // Base64 编码的内容
        let sha: String            // 文件的 Git SHA
        let encoding: String
        let path: String
    }

    /// 读取文件
    /// - Returns: (解 Base64 后的原始数据, sha)，文件不存在返回 nil
    func readFile(path: String) async throws -> (Data, String)? {
        let encodedPath = path.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed) ?? path
        let url = URL(string: "\(baseURL)/repos/\(owner)/\(repo)/contents/\(encodedPath)")!

        do {
            let (data, response) = try await performRequest(url: url, method: "GET")
            guard let http = response as? HTTPURLResponse else { return nil }
            if http.statusCode == 404 { return nil }
            guard http.statusCode == 200 else {
                throw GitHubError.httpError(http.statusCode, body: String(data: data, encoding: .utf8) ?? "")
            }
            let file = try JSONDecoder().decode(GitHubFile.self, from: data)
            // GitHub 返回的 content 可能包含换行符，需要先移除
            let cleaned = file.content.replacingOccurrences(of: "\n", with: "")
            guard let contentData = Data(base64Encoded: cleaned) else {
                throw GitHubError.invalidContent
            }
            return (contentData, file.sha)
        } catch {
            if let urlError = error as? URLError, urlError.code == .fileDoesNotExist {
                return nil
            }
            throw error
        }
    }

    // MARK: - 文件写入

    struct UploadPayload: Codable {
        let message: String
        let content: String        // Base64 编码
        let sha: String?           // 更新时必带
    }

    struct UploadResponse: Codable {
        let content: GitHubFile?
        let commit: CommitInfo?
    }

    struct CommitInfo: Codable {
        let sha: String
        let message: String
    }

    /// 上传或更新文件
    /// - Parameters:
    ///   - path: 文件路径（如 "data/messages.enc"）
    ///   - data: 文件二进制内容
    ///   - sha: 已知文件的 SHA（更新时必带，新建传 nil）
    ///   - message: commit message
    func writeFile(path: String, data: Data, sha: String?, message: String) async throws -> String {
        let encodedPath = path.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed) ?? path
        let url = URL(string: "\(baseURL)/repos/\(owner)/\(repo)/contents/\(encodedPath)")!

        let payload = UploadPayload(
            message: message,
            content: data.base64EncodedString(),
            sha: sha
        )

        var request = URLRequest(url: url)
        request.httpMethod = "PUT"
        request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        request.setValue("application/vnd.github+json", forHTTPHeaderField: "Accept")
        request.setValue("SLC-Lovers/1.0", forHTTPHeaderField: "User-Agent")
        request.httpBody = try JSONEncoder().encode(payload)

        let (responseData, response) = try await URLSession.shared.data(for: request)
        guard let http = response as? HTTPURLResponse else {
            throw GitHubError.invalidResponse
        }
        guard http.statusCode == 200 || http.statusCode == 201 else {
            let body = String(data: responseData, encoding: .utf8) ?? ""
            throw GitHubError.httpError(http.statusCode, body: body)
        }
        let resp = try JSONDecoder().decode(UploadResponse.self, from: responseData)
        return resp.commit?.sha ?? ""
    }

    /// 删除文件
    func deleteFile(path: String, sha: String, message: String) async throws {
        let encodedPath = path.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed) ?? path
        let url = URL(string: "\(baseURL)/repos/\(owner)/\(repo)/contents/\(encodedPath)")!

        var request = URLRequest(url: url)
        request.httpMethod = "DELETE"
        request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        request.setValue("application/vnd.github+json", forHTTPHeaderField: "Accept")
        request.setValue("SLC-Lovers/1.0", forHTTPHeaderField: "User-Agent")

        struct DeletePayload: Codable {
            let message: String
            let sha: String
        }
        request.httpBody = try JSONEncoder().encode(DeletePayload(message: message, sha: sha))

        let (_, response) = try await URLSession.shared.data(for: request)
        guard let http = response as? HTTPURLResponse else { return }
        guard http.statusCode == 200 else {
            throw GitHubError.httpError(http.statusCode, body: "")
        }
    }

    // MARK: - 私有

    private func performRequest(url: URL, method: String) async throws -> (Data, URLResponse) {
        var request = URLRequest(url: url)
        request.httpMethod = method
        request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        request.setValue("application/vnd.github+json", forHTTPHeaderField: "Accept")
        request.setValue("SLC-Lovers/1.0", forHTTPHeaderField: "User-Agent")
        return try await URLSession.shared.data(for: request)
    }
}

enum GitHubError: Error, LocalizedError {
    case invalidResponse
    case invalidContent
    case httpError(Int, body: String)

    var errorDescription: String? {
        switch self {
        case .invalidResponse: return "GitHub 返回无效响应"
        case .invalidContent: return "文件内容损坏"
        case .httpError(let code, let body): return "GitHub HTTP \(code): \(body.prefix(200))"
        }
    }
}