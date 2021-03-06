/**
 * Copyright (C) 2020-2021. Ricky Galliani. All Rights Reserved.
 * Email: pjgalliani@gmail.com
 */

package model.classification.votedperceptron

import data.normalize.{MinMaxNormalizer, Normalizer}
import example.UnitBinaryClassificationExample
import model.UnitBinaryClassificationModel
import model.classification.perceptron.{LogLevelSeed, Perceptron}
import org.apache.logging.log4j.Level

import scala.annotation.tailrec
import scala.util.Random

class VotedPerceptron(var weights: List[(List[Double], Double)] = List[(List[Double], Double)](),
                      val maxEpochs: Int = MaxEpochs,
                      override val normalizer: Option[Normalizer] = Some(new MinMaxNormalizer()),
                      override val verbose: Boolean = false) extends UnitBinaryClassificationModel {

  private val random = new Random
  val vp: Level = Level.forName("votedperceptron", LogLevelSeed)

  /**
   * Implements the Voted model.classification.perceptron.Perceptron learning algorithm:
   * - http://curtis.ml.cmu.edu/w/courses/index.php/Voted_Perceptron
   */
  override def learn(examples: List[UnitBinaryClassificationExample]): Unit = {
    random.setSeed(TrainSeed)
    random.shuffle(examples)
    val numExamples = examples.length
    if (numExamples == 0) throw new IllegalStateException("No training examples passed.")

    @tailrec
    def trainEpoch(epoch: Int, pocketWeightVotes: WeightVotes): Unit = {
      if (epoch >= maxEpochs) { weights = pocketWeightVotes.getFinalWeights }
      else {
        val randExampleInds = random.shuffle((0 until numExamples).toList)
        val curWeights = pocketWeightVotes.weights.head
        var curVotes = 0 // Number of examples this set of weights classifies correctly
        var stillPerfect = true
        while (stillPerfect && curVotes < numExamples) {
          val example = examples(randExampleInds(curVotes))
          val prediction = Perceptron.inference(curWeights, example.X)
          stillPerfect = prediction == example.y
          curVotes = if (stillPerfect) curVotes + 1 else curVotes
        }
        // current weights linearly separate the dataset
        if (stillPerfect) { weights = WeightVotes(List(curWeights), List[Int](1)).getFinalWeights }
        else {
          // Update current weights: w = w + x * y where (x, y) is a random misclassified example
          val mistakes = Perceptron.misclassifiedExamples(curWeights, examples)
          val numMistakes = mistakes.size
          val randomMistake = mistakes(random.nextInt(numMistakes))
          val dw = (List(1.0) ::: randomMistake.X).map(_ * randomMistake.y)
          val newWeights = curWeights.zip(dw).map { case (w, d) => w + d }
          // Add current weights to set of past weights
          val newWeightVotes = {
            if (curVotes == 0) { pocketWeightVotes }
            else {
              val newPocketWeights = newWeights :: curWeights :: pocketWeightVotes.weights.tail
              val newPocketVotes = 0 :: curVotes :: pocketWeightVotes.votes.tail
              WeightVotes(newPocketWeights, newPocketVotes)
            }
          }
          if (epoch % LogFrequency == 0 && verbose) { logger(vp, s"Epoch: $epoch, Mistakes: $numMistakes") }
          trainEpoch(epoch + 1, newWeightVotes)
        }
      }
    }

    // Training for the first time, initialize weights to 0.0 as only "voting" weights
    val modDim = examples.head.X.length + 1
    val pocketWeightVotes = WeightVotes(List(List.fill(modDim)(0.0)), List(1))
    trainEpoch(epoch = 1, pocketWeightVotes = pocketWeightVotes)
  }

  override def inference(X: List[Double]): Double = VotedPerceptron.inference(weights, X)

}

object VotedPerceptron {

  def inference(weights: List[(List[Double], Double)], x: List[Double]): Double = {
    val score = weights.map { case (w, v) => Perceptron.inference(w, x) * v }.sum
    if (score >= 0) 1 else -1
  }

}