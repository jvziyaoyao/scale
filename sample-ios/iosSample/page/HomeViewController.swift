//
//  HomeViewController.swift
//  iosSample
//
//  Created by 橘子妖妖 on 2025/6/28.
//


import SwiftUI
import SampleKmpKit

struct HomePageView: View {
    
    @EnvironmentObject var navigationManager: NavigationManager
    
    var body: some View {
        ComposeSwiperView {
            ComposeView {
                return ViewControllerKt.homeViewController(
                    onZoomable: {
                        navigationManager.next(page: .Zoomable)
                    },
                    onNormal: {
                        navigationManager.next(page: .Normal)
                    },
                    onHuge: {
                        navigationManager.next(page: .Huge)
                    },
                    onGallery: {
                        navigationManager.next(page: .Gallery)
                    },
                    onPreviewer: {
                        navigationManager.next(page: .Previewer)
                    },
                    onTransform: {
                        navigationManager.next(page: .Transform)
                    },
                    onDecoder: {
                        navigationManager.next(page: .Decoder)
                    },
                    onDuplicate: {
                        navigationManager.next(page: .Duplicate)
                    }
                )
            }
        }
    }
    
}

struct ZoomablePageView: View {
    @EnvironmentObject var navigationManager: NavigationManager
    var body: some View {
        ComposeSwiperView {
            ComposeView {
                return ViewControllerKt.zoomableViewController()
            }
        }
    }
}

struct NormalPageView: View {
    @EnvironmentObject var navigationManager: NavigationManager
    var body: some View {
        ComposeSwiperView {
            ComposeView {
                return ViewControllerKt.normalViewController()
            }
        }
    }
}


struct HugePageView: View {
    @EnvironmentObject var navigationManager: NavigationManager
    var body: some View {
        ComposeSwiperView {
            ComposeView {
                return ViewControllerKt.hugeViewController()
            }
        }
    }
}

struct GalleryPageView: View {
    @EnvironmentObject var navigationManager: NavigationManager
    var body: some View {
        ComposeSwiperView {
            ComposeView {
                return ViewControllerKt.galleryViewController()
            }
        }
    }
}

struct PreviewerPageView: View {
    @EnvironmentObject var navigationManager: NavigationManager
    var body: some View {
        ComposeSwiperView {
            ComposeView {
                return ViewControllerKt.previewerViewController()
            }
        }
    }
}

struct TransformPageView: View {
    @EnvironmentObject var navigationManager: NavigationManager
    var body: some View {
        ComposeSwiperView {
            ComposeView {
                return ViewControllerKt.transformViewController()
            }
        }
    }
}

struct DecoderPageView: View {
    @EnvironmentObject var navigationManager: NavigationManager
    var body: some View {
        ComposeSwiperView {
            ComposeView {
                return ViewControllerKt.decoderViewController()
            }
        }
    }
}

struct DuplicatePageView: View {
    @EnvironmentObject var navigationManager: NavigationManager
    var body: some View {
        ComposeSwiperView {
            ComposeView {
                return ViewControllerKt.duplicateViewController()
            }
        }
    }
}
