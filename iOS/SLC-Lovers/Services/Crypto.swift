import Foundation
import CryptoKit

/// 端到端加密工具
/// AES-GCM-256，密钥从配对码 + 配对 ID 派生
enum SLCCrypto {

    /// 从配对码 + 配对 ID 派生 32 字节密钥
    static func deriveKey(pairingCode: String, pairingId: String) -> SymmetricKey {
        let salt = Data("SLC-Lovers-v1-salt".utf8)
        let password = Data("\(pairingCode):\(pairingId)".utf8)

        // PBKDF2-HMAC-SHA256, 100k iterations
        let derivedKey = HKDF<SHA256>.deriveKey(
            inputKeyMaterial: SymmetricKey(data: password),
            salt: salt,
            info: Data("aes-256-key".utf8),
            outputByteCount: 32
        )
        return derivedKey
    }

    /// 加密
    /// 输出格式：Base64( IV(12) || Ciphertext || Tag(16) )
    static func encrypt(_ data: Data, key: SymmetricKey) throws -> String {
        let nonce = AES.GCM.Nonce()
        let sealedBox = try AES.GCM.seal(data, using: key, nonce: nonce)
        // sealedBox.combined = nonce || ciphertext || tag
        guard let combined = sealedBox.combined else {
            throw CryptoError.encryptionFailed
        }
        return combined.base64EncodedString()
    }

    /// 解密
    static func decrypt(_ base64String: String, key: SymmetricKey) throws -> Data {
        guard let combined = Data(base64Encoded: base64String) else {
            throw CryptoError.invalidBase64
        }
        let sealedBox = try AES.GCM.SealedBox(combined: combined)
        return try AES.GCM.open(sealedBox, using: key)
    }

    enum CryptoError: Error {
        case encryptionFailed
        case invalidBase64
    }
}