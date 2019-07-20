package jigg.ml.keras.Merlin

import jigg.util.HDF5Object
import jigg.ml.keras.Tanh

import breeze.linalg.{DenseMatrix,DenseVector}
import ucar.nc2.{Attribute,Group,NetcdfFile,Variable}

class MerlinModel(path: String) {
  private val model = HDF5Object.fromFile(path)
  private val Groupo = model.checkAndGetGroup("seg")
  private val Group2 = model.GetGroups_g("nn", Groupo)
  private val Group3 = model.GetGroups_g("g", Group2)
  private val inputdata = model.GetVariable_g("inids", Group3)
  private val outputdata = model.GetVariable_g("outids", Group3)
  private val Group4 = model.GetGroups_g("nodes", Group3)
  private var MerlinGroup = model.GetGroups_g("1", Group4)
  private val MerlinAttribute = model.GetAttributes_g("#JULIA_TYPE", MerlinGroup)

  var d_lst = List[String]()
  var f_lst = List[String]()

  var group_num = 0
  for (i <- 1 to 20) {
    MerlinGroup = model.GetGroups_g(i.toString, Group4)
    if(MerlinGroup == null){
      if(group_num == 0){
        group_num = i - 1
      }
    }
  }

  def choose_func1(f: String): String = {
    var a: String = "0"
    f match{
      case "nothing" =>  a = "nothing"
      case "Merlin.window" => a = "Merlin.window"
      case "Merlin.gemm" => a = "Merlin.gemm"
      case ".+" => a = ".+"
      case "tanh" => a = "tanh"
      case "Lookup" => a = "Lookup"
      case _ => a = "Lookup"
    }
    a 
  }

  def ucartoNormal(id:Int, g:Group, ii:Int=0, flagSame:Boolean=false): DenseMatrix[Float] = {
    val MGroup = model.GetGroups_g(id.toString, g)
    val d_data = model.GetVariable_g("data", MGroup)
    val r_temp = d_data.getDimensions().toArray
    val n_row = r_temp(0).toString.split(" ")(2).toString.split(";")(0).toInt
    val n_col = r_temp(1).toString.split(" ")(2).toString.split(";")(0).toInt

    var temp = d_lst(id-1).split(" ")
    if (flagSame) temp = d_lst(ii-2).split(" ")
    var mat = DenseMatrix.zeros[Float](n_row,n_col)
    var c = 0.toFloat

    for (j <- 0 to n_row-1){
      for (k <- 0 to n_col-1){
        if (!flagSame) c = temp(j*(n_col)+k).toFloat
        else if(flagSame) c = temp(k).toFloat
        mat(j,k) = c
      }
    }
    mat
  }

  def ucartoNormal_f(id:Int, g:Group, ii:Int=0, flagSame:Boolean=false): DenseMatrix[Float] = {
    val MGroup = model.GetGroups_g(id.toString, g)
    val d_data = model.GetVariable_g("f", MGroup)
    val r_temp = d_data.getDimensions().toArray
    val n_row = r_temp(0).toString.split(" ")(2).toString.split(";")(0).toInt
    val n_col = r_temp(1).toString.split(" ")(2).toString.split(";")(0).toInt
    var temp = f_lst(id-1).split(" ")
    if (flagSame) temp = d_lst(ii-2).split(" ")
    var mat = DenseMatrix.zeros[Float](n_row,n_col)
    var c = 0.toFloat

    var count = 0
    for (j <- 0 to n_row-1){
      for (k <- 0 to n_col-1){
        if (!flagSame) c = temp(j*(n_col)+k).toFloat
        else if(flagSame) c = temp(k).toFloat
        mat(j,k) = c
      }
    }
    mat
  }

  def Lookup(mat:DenseMatrix[Float], vocabulary:Int, outdim:Int): DenseMatrix[Float] = {
    val n_col = mat.cols
    var a = DenseMatrix.zeros[Float](n_col,outdim)
    for(i <- 0 to outdim-1) {
      for(j <- 0 to n_col-1){
        a(j,i) = mat(i,j)
      }
    }
    a
  }

  def window(x:DenseMatrix[Float], dims:Int, pads:Int, strides:Int):DenseMatrix[Float] = {
    val N = x.size
    val xx = x.toDenseVector
    val c:Int = (x.size + 2*pads - dims) / strides + 1
    var a = DenseVector.zeros[Float](dims*c)
    var b = DenseVector.zeros[Float](pads+N+strides)
    for (i <- 0 to N-1){
      b(i+pads) = xx(i)
    }
    var count = 0
    for (i <- 0 to a.size-1){
      if (count==b.size){
        count = 0
      }
      a(i) = b(count)
      count +=1
    }
    var d = DenseMatrix.zeros[Float](dims,c)
    for (i<- 0 to dims-1){
      for(j<- 0 to c-1){
        d(i,j) = a(i+(j-1)*dims)
      }
    }
    d
  }

  def constructMerlinNetwork():List[DenseMatrix[Float]] = {
    var res = List[DenseMatrix[Float]]()
    var resM = List[DenseMatrix[Float]]()
    var resV = List[List[DenseVector[Float]]]()

    for (i <- 1 to group_num) {
      MerlinGroup = model.GetGroups_g(i.toString, Group4)
      var d_data = model.GetVariable_g("data", MerlinGroup).read.toString
      val d_f = model.GetVariable_g("f", MerlinGroup).read.toString
      val MerlinGroup_arg = model.GetGroups_g("args", MerlinGroup)
      val x:String = choose_func1(d_f)
      val Group5 = model.GetGroups_g(i.toString, Group4)
      val Group5_arg = model.GetGroups_g("args", Group5)
      d_lst :+= d_data
      f_lst :+= d_f

      var d_data_t = DenseMatrix(0.toFloat)
      try {
        d_data_t = ucartoNormal(i, Group4)
      }
      catch {
        case e: Exception => d_data_t = DenseMatrix(0.toFloat)
      }

      if (x=="nothing") {
        res :+= d_data_t
      } else if (x == "Lookup") {
        val nid = model.GetVariable_g(1.toString, Group5_arg).read.toString.split(" ")
        val Group6 = model.GetGroups_g(nid(0).toString,Group4)
        val x = model.GetVariable_g("data", Group6).read.toString.split(" ")
        val voca = x(0).toInt
        val outd = x(1).toInt
        val f_now = d_f
        val mat = ucartoNormal_f(i , Group4)

        val ans4 = Lookup(mat,voca,outd)
        res :+= ans4

      } else if (x == "Merlin.window"){
        val Group52 = model.GetGroups_g(2.toString, Group5_arg)
        val Group53 = model.GetGroups_g(3.toString, Group5_arg)
        val Group54 = model.GetGroups_g(4.toString, Group5_arg)
        val variable_arg2 = model.GetVariable_g(1.toString, Group52).read.toString.split(" ")
        val variable_arg3 = model.GetVariable_g(1.toString, Group53).read.toString.split(" ")
        val variable_arg4 = model.GetVariable_g(1.toString, Group54).read.toString.split(" ")
        val indim = variable_arg2(0).toInt
        val pads = variable_arg3(0).toInt
        val strides = variable_arg4(0).toInt
        val x = res(i-2)
        val win = window(x,indim,pads,strides)

        res :+= win

      } else if (x == "Merlin.gemm"){
        val variable_arg3 = model.GetVariable_g(3.toString, Group5_arg).read.toString.split(" ")
        val variable_arg4 = model.GetVariable_g(4.toString, Group5_arg).read.toString.split(" ")
        val variable_arg5 = model.GetVariable_g(5.toString, Group5_arg).read.toString.split(" ")
        val int1 = variable_arg3(0).toInt
        val nid2 = variable_arg4(0).toInt
        val nid3 = variable_arg5(0).toInt
        val mat1 = res(nid2-1)
        val mat2_t = res(nid3-1)
        val mat2 = mat2_t.t
        val matres_t = mat2 * mat1
        val matres = matres_t.t

        res :+= matres

      } else if (x == ".+" ) {
        val variable_arg1 = model.GetVariable_g(1.toString, Group5_arg).read.toString.split(" ")
        val variable_arg2 = model.GetVariable_g(2.toString, Group5_arg).read.toString.split(" ")
        val nid1 = variable_arg1(0).toInt
        val nid2 = variable_arg2(0).toInt

        val mat1 = res(nid1-1)
        val mat2 = ucartoNormal(nid2, Group4)

        var matres = DenseMatrix.zeros[Float](mat1.rows, mat1.cols)
        for(jj <- 0 to mat1.cols-1){
          for(ii <- 0 to mat1.rows-1){
            matres(ii,jj) = mat1(ii,jj) + mat2(0,jj)
          }
        }

        res :+= matres

      } else if (x == "tanh") {
        var matbefore = res(i-2)

        matbefore = Tanh(matbefore)
        res :+= matbefore
      }
    }
    res
  }

  val net = constructMerlinNetwork()
  println(net)
}

object MerlinModel{
  def apply(path: String): MerlinModel = new MerlinModel(path)
}

