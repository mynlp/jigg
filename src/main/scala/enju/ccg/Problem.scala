package enju.ccg

trait Problem {
  def train: Unit
  def predict: Unit
  def evaluate: Unit
  def save: Unit
}
