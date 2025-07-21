//
//  ComposeView.swift
//  iosSample
//
//  Created by 橘子妖妖 on 2025/6/27.
//

import UIKit
import SwiftUI
import SampleKmpKit

struct ComposeView: UIViewControllerRepresentable {

    var getViewController: () -> UIViewController
    
    func makeUIViewController(context: Context) -> UIViewController {
        return getViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}


struct NavigationStackGestureEnabler: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        let vc = UIViewController()
        DispatchQueue.main.async {
            if let navigationController = vc.navigationController {
                navigationController.interactivePopGestureRecognizer?.delegate = nil
                navigationController.interactivePopGestureRecognizer?.isEnabled = true
            }
        }
        return vc
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}


struct ComposeSwiperView<Content: View>: View {
    let content: () -> Content
    let barWidth: CGFloat
    let needSwiper: Bool

    init(barWidth: CGFloat = 20, needSwiper: Bool = true, @ViewBuilder content: @escaping () -> Content) {
        self.content = content
        self.barWidth = barWidth
        self.needSwiper = needSwiper
    }

    var body: some View {
        GeometryReader { geometry in
            ZStack(alignment: .leading) {
                content()

                if needSwiper {
                    Rectangle()
                        .foregroundColor(.white)
                        .opacity(0.012)
                        .frame(width: barWidth, height: geometry.size.height)
                        .contentShape(Rectangle())
                        .allowsHitTesting(true)
                }
            }
            .ignoresSafeArea(.all, edges: .all)
            .toolbar(.hidden)
            .background(NavigationStackGestureEnabler())
        }
    }
}
