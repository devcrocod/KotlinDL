/*
 * Copyright 2020 JetBrains s.r.o. and Kotlin Deep Learning project contributors. All Rights Reserved.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE.txt file.
 */

package examples.custom

import org.jetbrains.kotlinx.dl.api.core.Sequential
import org.jetbrains.kotlinx.dl.api.core.activation.Activations
import org.jetbrains.kotlinx.dl.api.core.callback.Callback
import org.jetbrains.kotlinx.dl.api.core.history.*
import org.jetbrains.kotlinx.dl.api.core.initializer.HeNormal
import org.jetbrains.kotlinx.dl.api.core.layer.Dense
import org.jetbrains.kotlinx.dl.api.core.layer.Flatten
import org.jetbrains.kotlinx.dl.api.core.layer.Input
import org.jetbrains.kotlinx.dl.api.core.layer.twodim.Conv2D
import org.jetbrains.kotlinx.dl.api.core.layer.twodim.ConvPadding
import org.jetbrains.kotlinx.dl.api.core.layer.twodim.MaxPool2D
import org.jetbrains.kotlinx.dl.api.core.loss.Losses
import org.jetbrains.kotlinx.dl.api.core.metric.Metrics
import org.jetbrains.kotlinx.dl.api.core.optimizer.Adam
import org.jetbrains.kotlinx.dl.datasets.Dataset
import org.jetbrains.kotlinx.dl.datasets.handlers.*

private const val EPOCHS = 1
private const val TRAINING_BATCH_SIZE = 500
private const val TEST_BATCH_SIZE = 1000
private const val NUM_CHANNELS = 1L
private const val IMAGE_SIZE = 28L

/**
 * This is an CNN based on an implementation of LeNet-5 from classic paper trained with custom callback.
 *
 * @see <a href="http://yann.lecun.com/exdb/publis/pdf/lecun-98.pdf">
 *    Gradient-based learning applied to document recognition:[Yann LeCun, Léon Bottou, Yoshua Bengio, Patrick Haffner, 1998]</a>
 */
private val model = Sequential.of(
    Input(
        IMAGE_SIZE,
        IMAGE_SIZE,
        NUM_CHANNELS
    ),
    Conv2D(
        filters = 32,
        kernelSize = longArrayOf(3, 3),
        strides = longArrayOf(1, 1, 1, 1),
        activation = Activations.Relu,
        kernelInitializer = HeNormal(),
        biasInitializer = HeNormal(),
        padding = ConvPadding.SAME
    ),
    MaxPool2D(
        poolSize = intArrayOf(1, 2, 2, 1),
        strides = intArrayOf(1, 2, 2, 1)
    ),
    Conv2D(
        filters = 64,
        kernelSize = longArrayOf(3, 3),
        strides = longArrayOf(1, 1, 1, 1),
        activation = Activations.Relu,
        kernelInitializer = HeNormal(),
        biasInitializer = HeNormal(),
        padding = ConvPadding.SAME
    ),
    MaxPool2D(
        poolSize = intArrayOf(1, 2, 2, 1),
        strides = intArrayOf(1, 2, 2, 1)
    ),
    Flatten(), // 3136
    Dense(
        outputSize = 512,
        activation = Activations.Relu,
        kernelInitializer = HeNormal(),
        biasInitializer = HeNormal()
    ),
    Dense(
        outputSize = NUMBER_OF_CLASSES,
        activation = Activations.Linear,
        kernelInitializer = HeNormal(),
        biasInitializer = HeNormal()
    )
)

fun main() {
    val (train, test) = Dataset.createTrainAndTestDatasets(
        TRAIN_IMAGES_ARCHIVE,
        TRAIN_LABELS_ARCHIVE,
        TEST_IMAGES_ARCHIVE,
        TEST_LABELS_ARCHIVE,
        NUMBER_OF_CLASSES,
        ::extractImages,
        ::extractLabels
    )

    model.use {
        it.compile(
            optimizer = Adam(),
            loss = Losses.SOFT_MAX_CROSS_ENTROPY_WITH_LOGITS,
            metric = Metrics.ACCURACY,
            callback = CustomCallback()
        )

        println(it.kGraph)

        it.fit(dataset = train, epochs = EPOCHS, batchSize = TRAINING_BATCH_SIZE, verbose = true)

        val accuracy = it.evaluate(dataset = test, batchSize = TEST_BATCH_SIZE).metrics[Metrics.ACCURACY]

        println("Accuracy: $accuracy")
    }
}

/**
 *
 */
class CustomCallback : Callback() {
    override fun onEpochBegin(epoch: Int, logs: TrainingHistory) {
        println("Epoch $epoch begins.")
    }

    override fun onEpochEnd(epoch: Int, event: EpochTrainingEvent, logs: TrainingHistory) {
        println("Epoch $epoch ends.")
    }

    override fun onTrainBatchBegin(batch: Int, batchSize: Int, logs: TrainingHistory) {
        println("Training batch $batch begins.")
    }

    override fun onTrainBatchEnd(batch: Int, batchSize: Int, event: BatchTrainingEvent, logs: TrainingHistory) {
        println("Training batch $batch ends with loss ${event.lossValue}.")
    }

    override fun onTrainBegin() {
        println("Train begins")
    }

    override fun onTrainEnd(logs: TrainingHistory) {
        println("Train ends with last loss ${logs.lastBatchEvent().lossValue}")
    }

    override fun onTestBatchBegin(batch: Int, batchSize: Int, logs: History) {
        println("Test batch $batch begins.")
    }

    override fun onTestBatchEnd(batch: Int, batchSize: Int, event: BatchEvent?, logs: History) {
        println("Test batch $batch ends with loss ${event!!.lossValue}..")
    }

    override fun onTestBegin() {
        println("Test begins")
    }

    override fun onTestEnd(logs: History) {
        println("Train ends with last loss ${logs.lastBatchEvent().lossValue}")
    }

    override fun onPredictBatchBegin(batch: Int, batchSize: Int) {
        println("Prediction batch $batch begins.")
    }

    override fun onPredictBatchEnd(batch: Int, batchSize: Int) {
        println("Prediction batch $batch ends.")
    }

    override fun onPredictBegin() {
        println("Train begins")
    }

    override fun onPredictEnd() {
        println("Test begins")
    }
}
