/* Copyright 2019 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package com.example.oppoapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.ConditionVariable;

import org.tensorflow.lite.examples.transfer.api.ModelLoader;
import org.tensorflow.lite.examples.transfer.api.TransferLearningModel;
import org.tensorflow.lite.examples.transfer.api.TransferLearningModel.LossConsumer;
import org.tensorflow.lite.examples.transfer.api.TransferLearningModel.AccConsumer;
import org.tensorflow.lite.examples.transfer.api.TransferLearningModel.Prediction;

import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * App-layer wrapper for {@link TransferLearningModel}.
 *
 * <p>This wrapper allows to run training continuously, using start/stop API, in contrast to
 * run-once API of {@link TransferLearningModel}.
 */
public class TransferLearningModelWrapper implements Closeable, Serializable {
  public static final int IMAGE_SIZE = 224;

  private final TransferLearningModel model;
  private int epochs;

  private final ConditionVariable shouldTrain = new ConditionVariable();
  private volatile LossConsumer lossConsumer;
  private volatile AccConsumer accConsumer;
  private Utils imageUtils = new Utils();

  TransferLearningModelWrapper(String parentDir, List<String> list) {
//    Arrays.asList("1", "2", "3", "4")
    model =
        new TransferLearningModel(
            new ModelLoader(parentDir, "model"), list);

    new Thread(() -> {
//      while (!Thread.interrupted()) {
        shouldTrain.block();
        try {
          model.train(epochs, lossConsumer, accConsumer).get();
        } catch (ExecutionException e) {
          throw new RuntimeException("Exception occurred during model training", e.getCause());
        } catch (InterruptedException e) {
          // no-op
        }
//      }
    }).start();
  }

  // This method is thread-safe.
  public Future<Void> addSample(float[][][] image, String className) {
    return model.addSample(image, className);
  }

  public void addBatchSample(String dirPath) throws FileNotFoundException {

    File dir = new File(dirPath);
    File[] listOfFiles = dir.listFiles();
    for (File childDir : listOfFiles) {
      if (childDir.isDirectory()) {
        String className = childDir.getName();
        File[] imageFiles = childDir.listFiles();
        for (File imageFile : imageFiles) {
          FileInputStream fis = new FileInputStream(imageFile);
          Bitmap bitmap = BitmapFactory.decodeStream(fis);
          float[][][] rgbImage = imageUtils.prepareCameraImage(bitmap, 0);
          model.addSample(rgbImage, className);
        }
      }
    }
  }

  // This method is thread-safe, but blocking.
  public Prediction[] predict(float[][][] image) {
    return model.predict(image);
  }

  public int getTrainBatchSize() {
    return model.getTrainBatchSize();
  }

  /**
   * Start training the model continuously until {@link #disableTraining() disableTraining} is
   * called.
   *
   * @param lossConsumer callback that the loss values will be passed to.
   */
  public void enableTraining(LossConsumer lossConsumer,AccConsumer accConsumer) {
    this.lossConsumer = lossConsumer;
    this.accConsumer = accConsumer;
    shouldTrain.open();
  }

  /**
   * Stops training the model.
   */
  public void disableTraining() {
    shouldTrain.close();
  }

  /**
   * Export the trained weights as a checkpoint file.
   * @param dirPath
   */
  public void saveModel(String dirPath){
    model.saveModel(dirPath);
  }

  /** Frees all model resources and shuts down all background threads. */
  public void close() {
    model.close();
  }

  public void setEpochs(int epochs) {
    this.epochs = epochs;
  }
}
