package com.jvziyaoyao.scale.image.sampling

import com.jvziyaoyao.scale.image.viewer.ModelProcessorPair

val samplingProcessorPair: ModelProcessorPair = SamplingDecoder::class to { model, state ->
    SamplingCanvas(
        samplingDecoder = model as SamplingDecoder,
        viewPort = state.getViewPort(),
    )
}