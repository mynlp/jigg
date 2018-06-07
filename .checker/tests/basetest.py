from unittest import TestCase
from typing import List

import subprocess
import sys
import time

class BaseTest(TestCase):
    '''
    '''
    def check_equal(self,
                    exe: List[str],
                    input_text: str,
                    expected_text: str,
                    output_file_ext='.xml'):
        '''check the expected text and the result text are equal.
        '''
        # The constants 
        self.input_file_name = 'input_.txt'
        self.output_file_name = self.input_file_name + output_file_ext

        # Make an input file `input_.txt`
        with open(self.input_file_name, mode='w', encoding='utf-8') as f:
            f.write(input_text)

        # the command option
        exe.append('-file')
        exe.append(self.input_file_name)

        # The execution of the command `exe`. The output file is generated.
        p = subprocess.Popen(exe).wait()

        # Read output file
        result_text = ''
        with open(self.output_file_name, mode='r', encoding='utf-8') as f:
            result_text = f.read()

        # Remove the input file and the output file
        subprocess.run(['rm', self.input_file_name, self.output_file_name])
        
        self.assertEqual(expected_text, result_text)
