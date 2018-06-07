from unittest import TestCase

import subprocess
import sys
import time

from basetest import BaseTest
from constant import (
    JIGG_JAR,
    JIGG_MODEL_JAR,
    CORENLP_JAR,
    CORENLP_MODEL_JAR
)


class TestName(BaseTest):

    def setUp(self):
        ## Set an input text
        self.input_text = ""

        ## Set an expected text
        self.expected_text = ""

        ## Set the annotators
        # For example,
        # jigg: 'corenlp[tokenize,ssplit,pos]'
        # corenlp: `tokenize,ssplit,pos`
        self.annotators = 'corenlp[tokenize]'

        ## Set a class path
        # When you set the class path to the current directory (variable `current_dir`),
        # please set the element of the `jar_files` list to empty.
        # If you specify the class path directory, please change the `current_dir`        
        jar_files = [JIGG_JAR, JIGG_MODEL_JAR]
        current_dir = "*"        
        self.classpath = ':'.join(jar_files) if len(jar_files) > 0 else current_dir

        ## Set a pipeline command (?)
        # For example,
        # jigg: 'jigg.pipeline.Pipeline'
        # corenlp: 'edu.stanford.nlp.pipeline.StanfordCoreNLP'
        self.pipeline = "jigg.pipeline.Pipeline"
        
        ## Set a execution command
        # The execution command is defined with the list type as the following.
        self.exe = ['java',
                    '-cp', self.classpath,
                    self.pipeline,
                    '-annotators', self.annotators]
        
    def test_tokenize(self):
        input_text = self.input_text
        expected_text = self.expected_text
        annotators = self.annotators
        exe = self.exe
        
        self.check_equal(exe, input_text, expected_text)
