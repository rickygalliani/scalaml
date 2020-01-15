import scala.util.Random

class Perceptron(var weights: List[Double] = List[Double]()) {

  private val random = new Random

  def train(examples: List[Example]): List[Double] = {
    def trainRecurse(examples: List[Example],
                     numExamples: Int,
                     curIteration: Int,
                     maxIterations: Int,
                     bestWeightsSoFar: List[Double],
                     fewestMistakesSoFar: Int): List[Double] = {
      if (curIteration > maxIterations) {
        weights = bestWeightsSoFar
        return weights
      }
      if (linearlySeparates(examples)) return weights
      // Update weights: w = w + x * y where (x, y) is a random misclassified example
      val mistakes = misclassifiedExamples(examples)
      val numMistakes = mistakes.length
      // println(s"weights = ${weights}")
      // println(s"bestWeightsSoFar = $bestWeightsSoFar")
      // println(s"fewestMistakesSoFar = $fewestMistakesSoFar")
      // println(s"numMistakes = $numMistakes\n")
      // Prepare "pocket" data for next training step
      val (newBestWeightsSoFar, newFewestMistakesSoFar) = {
        if (numMistakes <= fewestMistakesSoFar) (weights, numMistakes)
        else (bestWeightsSoFar, fewestMistakesSoFar)
      }
      val randomMistake = mistakes(random.nextInt(numMistakes))
      val dw = (List(1.0) ::: randomMistake.featureVector).map(_ * randomMistake.label)
      weights = weights.zip(dw).map { case (w, d) => w + d }
      trainRecurse(examples,
                   numExamples,
                   curIteration + 1,
                   maxIterations,
                   newBestWeightsSoFar,
                   newFewestMistakesSoFar)
    }
    val numExamples = examples.length
    if (numExamples == 0) throw new IllegalStateException("No training examples passed.")
    // Training for the first time, initialize weights to 0.0, including bias term as w_0
    weights = List.fill(examples(0).featureVector.length + 1)(0.0)
    trainRecurse(examples, numExamples, 1, numExamples * 10, weights, numExamples)
  }

  def predict(example: Example): Int = Perceptron.predict(weights, example)

  def linearlySeparates(examples: List[Example]): Boolean = {
    Perceptron.linearlySeparates(weights, examples)
  }

  def misclassifiedExamples(examples: List[Example]): List[Example] = {
    Perceptron.misclassifiedExamples(weights, examples)
  }

}

object Perceptron {

  def predict(weights: List[Double], example: Example): Int = {
    val X = List(1.0) ::: example.featureVector
    val xDim = X.length
    val modDim = weights.length
    if (xDim != modDim) {
      throw new IllegalStateException(
        s"Dimension of feature vector (${xDim}) and dimension of model (${modDim}) don't match."
      )
    }
    val score = weights.zip(X).map { case (w, v) => w * v }.sum
    if (score >= 0) 1 else -1
  }

  def linearlySeparates(weights: List[Double], examples: List[Example]): Boolean = {
    !examples.exists(ex => Perceptron.predict(weights, ex) != ex.label)
  }

  def misclassifiedExamples(weights: List[Double], examples: List[Example]): List[Example] = {
    examples.filter(ex => Perceptron.predict(weights, ex) != ex.label)
  }

}
