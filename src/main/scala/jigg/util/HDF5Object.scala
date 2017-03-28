package jigg.util

/*
 Copyright 2013-2015 Hiroshi Noji
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
     http://www.apache.org/licencses/LICENSE-2.0
     
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitation under the License.
*/

import ucar.nc2.{Attribute, Group, NetcdfFile}

class HDF5Object(path: String) {

  val file:NetcdfFile = NetcdfFile.open(path, null)

  val rootGroup:Group = file.getRootGroup

  def checkAndGetAttribute(name: String): Attribute = Option(rootGroup.findAttribute(name)) match {
    case Some(x) => x
    case None => throw new IllegalArgumentException("cannot get " + name + " attribute from input model file")
  }

  def checkAndGetGroup(name: String): Group = Option(rootGroup.findGroup(name)) match {
    case Some(x) => x
    case None => throw new IllegalArgumentException("cannot get " + name + " group from input model file")
  }

}

object HDF5Object{
  def apply(path: String): HDF5Object = new HDF5Object(path)
}
