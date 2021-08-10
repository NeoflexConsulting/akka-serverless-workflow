package org.fluminous.matrix

sealed trait ServicesWithInput[I] {
  type THIS <: ServicesWithInput[I]
  def append[O](service: Service[I, O]): ServicesWithInputCompose[I, O, THIS]
  def toSeq: Seq[Service[I, _]] = constructSeq(Seq.empty)
  def constructSeq(currentSeq: Seq[Service[I, _]]): Seq[Service[I, _]]
}

sealed class ServicesWithInputNil[I] extends ServicesWithInput[I] {
  type THIS = ServicesWithInputNil[I]
  override def append[O](service: Service[I, O]): ServicesWithInputCompose[I, O, THIS] =
    ServicesWithInputCompose(service, ServicesWithInputNil[I])
  override def constructSeq(currentSeq: Seq[Service[I, _]]): Seq[Service[I, _]] = currentSeq
}

object ServicesWithInputNil {
  def apply[I] = new ServicesWithInputNil[I]
}

final case class ServicesWithInputCompose[I, O, TAIL <: ServicesWithInput[I]](head: Service[I, O], tail: TAIL)
    extends ServicesWithInput[I] {
  type THIS = ServicesWithInputCompose[I, O, TAIL]
  override def append[NEXT_O](service: Service[I, NEXT_O]): ServicesWithInputCompose[I, NEXT_O, THIS] =
    ServicesWithInputCompose(service, this)
  def getByOutput[SEARCHED](implicit get: GetServiceByOutput[I, SEARCHED, THIS]): Service[I, SEARCHED] = get.apply(this)

  override def constructSeq(currentSeq: Seq[Service[I, _]]): Seq[Service[I, _]] = tail.constructSeq(head +: currentSeq)
}

object ServicesWithInput {
  def apply[A, H](service: Service[A, H]) = {
    ServicesWithInputNil[A].append(service)
  }
}
