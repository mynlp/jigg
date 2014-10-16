#!/usr/bin/env python
# -*- coding: utf-8 -*-

from xml.etree.ElementTree import *
import xmlrpclib, sys
from pprint import pprint

reload(sys)
sys.setdefaultencoding('utf-8')

class PipelineClient:
    def __init__(self):
        self.server = xmlrpclib.ServerProxy('http://127.0.0.1:8080')
    
    def parse(self, text):
        return fromstring(self.server.parse(text))

pipeline = PipelineClient()
result = pipeline.parse("今日はいい天気ですね。")

print tostring(result, encoding='utf-8')
