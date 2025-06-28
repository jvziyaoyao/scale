//
//  NavigationPageView.swift
//  iosSample
//
//  Created by 橘子妖妖 on 2025/6/28.
//

import SampleKmpKit
import SwiftUI

class NavigationManager: ObservableObject {
    @Published var path = NavigationPath()

    func next(page: Page) {
        path.append(page)
    }

    func back() {
        DispatchQueue.main.async {
            if self.path.count > 0 {
                self.path.removeLast()
            }
        }
    }

    func goHome() {
        while path.count > 0 {
            path.removeLast(path.count)
        }
    }
}

struct NavigationPageView<Content: View>: View {
    @StateObject private var navigationManager = NavigationManager()

    var content: () -> Content

    var body: some View {
        NavigationStack(path: $navigationManager.path) {
            ZStack {
                self.content()
            }
            .navigationDestination(for: Page.self) { target in
                target.view
            }
        }
        .environmentObject(navigationManager)
        .ignoresSafeArea(.all, edges: .all)
        .toolbar(.hidden)
    }
}

enum Page: Hashable {
    case Home
    case Zoomable
    case Normal
    case Huge
    case Gallery
    case Previewer
    case Transform
    case Decoder
    case Duplicate

    @ViewBuilder
    var view: some View {
        switch self {
        case .Home:
            HomePageView()
        case .Zoomable:
            ZoomablePageView()
        case .Normal:
            NormalPageView()
        case .Huge:
            HugePageView()
        case .Gallery:
            GalleryPageView()
        case .Previewer:
            PreviewerPageView()
        case .Transform:
            TransformPageView()
        case .Decoder:
            DecoderPageView()
        case .Duplicate:
            DuplicatePageView()
        }
    }
}

struct NotFoundPageView: View {
    var body: some View {
        Text("页面找不到～")
    }
}

struct RoomTestPage: View {
    var body: some View {
        Text("好家伙")
    }
}
