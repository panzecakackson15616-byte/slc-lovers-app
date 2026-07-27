import SwiftUI

/// 共享相册
struct GalleryView: View {
    @EnvironmentObject var appState: AppState
    @State private var showAddSheet = false

    private let columns = [
        GridItem(.flexible(), spacing: SLCSpace.xs),
        GridItem(.flexible(), spacing: SLCSpace.xs),
        GridItem(.flexible(), spacing: SLCSpace.xs),
    ]

    var body: some View {
        NavigationView {
            ScrollView {
                if appState.photos.isEmpty {
                    SLCEmptyView(
                        icon: "photo.on.rectangle.angled",
                        title: "还没有照片",
                        subtitle: "把你们的回忆都装进来吧"
                    )
                    .padding(.top, 80)
                } else {
                    LazyVGrid(columns: columns, spacing: SLCSpace.xs) {
                        ForEach(appState.photos) { photo in
                            PhotoThumb(photo: photo)
                        }
                    }
                    .padding(.horizontal, SLCSpace.xs)
                    .padding(.top, SLCSpace.sm)
                }
            }
            .background(SLCColor.cream)
            .navigationTitle("相册")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button {
                        Haptics.impact(.light)
                        showAddSheet = true
                    } label: {
                        Image(systemName: "plus.circle.fill")
                            .font(.system(size: 24))
                            .foregroundColor(SLCColor.him)
                    }
                }
            }
            .sheet(isPresented: $showAddSheet) {
                AddPhotoSheet()
            }
        }
    }
}

private struct PhotoThumb: View {
    let photo: Photo
    @State private var showDetail = false

    var body: some View {
        Button {
            Haptics.selection()
            showDetail = true
        } label: {
            Rectangle()
                .fill(SLCColor.creamDeep)
                .aspectRatio(1, contentMode: .fit)
                .overlay(
                    Image(systemName: "photo")
                        .foregroundColor(SLCColor.textTertiary)
                )
        }
        .buttonStyle(.plain)
        .sheet(isPresented: $showDetail) {
            PhotoDetailSheet(photo: photo)
        }
    }
}

private struct PhotoDetailSheet: View {
    let photo: Photo
    @Environment(\.dismiss) var dismiss

    var body: some View {
        VStack(spacing: SLCSpace.md) {
            HStack {
                Spacer()
                Button("关闭") { dismiss() }
                    .foregroundColor(SLCColor.him)
                    .padding()
            }
            Rectangle()
                .fill(SLCColor.creamDeep)
                .aspectRatio(1, contentMode: .fit)
                .overlay(
                    Image(systemName: "photo.fill")
                        .font(.system(size: 80))
                        .foregroundColor(SLCColor.textTertiary)
                )
                .padding()
            if let caption = photo.caption {
                Text(caption)
                    .font(SLCFont.body(SLCFontSize.bodyLarge))
                    .foregroundColor(SLCColor.textPrimary)
                    .padding()
            }
            Text(DateUtils.fullChinese(photo.takenAt))
                .font(SLCFont.caption(SLCFontSize.bodySmall))
                .foregroundColor(SLCColor.textSecondary)
            Spacer()
        }
        .background(SLCColor.cream)
    }
}

private struct AddPhotoSheet: View {
    @EnvironmentObject var appState: AppState
    @Environment(\.dismiss) var dismiss
    @State private var caption = ""

    var body: some View {
        NavigationView {
            VStack(spacing: SLCSpace.lg) {
                Rectangle()
                    .fill(SLCColor.creamDeep)
                    .frame(height: 280)
                    .overlay(
                        VStack {
                            Image(systemName: "photo.fill")
                                .font(.system(size: 60))
                                .foregroundColor(SLCColor.textTertiary)
                            Text("点击上传照片")
                                .font(SLCFont.body(SLCFontSize.bodyMedium))
                                .foregroundColor(SLCColor.textSecondary)
                        }
                    )
                    .clipShape(RoundedRectangle(cornerRadius: SLCRadius.lg))

                TextField("写点什么...", text: $caption, axis: .vertical)
                    .lineLimit(3...6)
                    .padding()
                    .background(SLCColor.creamLight)
                    .clipShape(RoundedRectangle(cornerRadius: SLCRadius.md))

                Spacer()
            }
            .padding(SLCSpace.md)
            .background(SLCColor.cream)
            .navigationTitle("添加照片")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("取消") { dismiss() }
                        .foregroundColor(SLCColor.textSecondary)
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("完成") {
                        if let userId = appState.currentUser?.id {
                            // 原型：用一个空图片数据占位
                            let placeholder = Data([0])
                            let photo = Photo(
                                uploaderId: userId,
                                imageData: placeholder,
                                caption: caption.isEmpty ? nil : caption
                            )
                            appState.addPhoto(photo)
                        }
                        dismiss()
                    }
                    .foregroundColor(SLCColor.him)
                    .fontWeight(.semibold)
                }
            }
        }
    }
}

#Preview {
    GalleryView()
        .environmentObject(AppState.shared)
}