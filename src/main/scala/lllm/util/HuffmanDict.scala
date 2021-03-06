package lllm.util

import breeze.linalg.Counter
import scala.collection.mutable
import breeze.util.Index

/**
 * @author jda
 */
case class HuffmanNode[L](weight: Double,
                          label: Option[L],
                          left: Option[HuffmanNode[L]],
                          right: Option[HuffmanNode[L]]) extends Ordered[HuffmanNode[L]] {

  def compare(that: HuffmanNode[L]): Int = -(this.weight compare that.weight)

}

case class HuffmanDict[L](dict: Map[L,String], prefixIndex: Index[String]) {
  final val constIndex = prefixIndex(HuffmanDict.Const)
}

object HuffmanDict {

  final def Const = "CONST"

  def fromCounts[L](counts: Iterable[(L,Double)]): HuffmanDict[L] = {
    val pq = mutable.PriorityQueue[HuffmanNode[L]]()
    counts.foreach { case (word, count) => pq.enqueue(HuffmanNode(count, Some(word), None, None)) }
    while (pq.size > 1) {
      val n1 = pq.dequeue()
      val n2 = pq.dequeue()
      val newNode = HuffmanNode(n1.weight + n2.weight, None, Some(n1), Some(n2))
      pq.enqueue(newNode)
    }

    val root = pq.dequeue()
    assert(pq.isEmpty)
    val dict = leavesAndPaths(root).toMap
    val prefixIndex = Index(prefixes(root) ++ Seq(Const))
    HuffmanDict(dict, prefixIndex)
  }

  // TODO(jda) these should be inside of HuffmanNode

  def leavesAndPaths[L](node: HuffmanNode[L], prefix: String = ""): Iterable[(L,String)] = {
    if (node.label.isDefined)
      Iterable((node.label.get, prefix))
    else
      leavesAndPaths(node.left.get, "0" + prefix) ++ leavesAndPaths(node.right.get, "1" + prefix)
  }

  def prefixes[L](node: HuffmanNode[L], prefix: String = ""): Iterable[String] = {
    if (node.label.isDefined)
      Iterable()
    else
      Iterable(prefix) ++ prefixes(node.left.get, "0" + prefix) ++ prefixes(node.right.get, "1" + prefix)
  }
}
