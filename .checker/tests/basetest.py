from unittest import TestCase
import subprocess
import xml.etree.ElementTree as ET

from comparison import elements_equal


class BaseTest(TestCase):
    '''
    '''
    def check_equal(self,
                    exe: str,
                    input_text: str,
                    expected_text: str):
        '''check the expected text and the result text are equal.
        '''
        # The constants
        self.input_file_name = 'input_.txt'
        self.output_file_name = self.input_file_name + '.xml'

        # Write to the input file `input_.txt`
        with open(self.input_file_name, mode='w', encoding='utf-8') as f:
            f.write(input_text)

        # Add the command option
        exe = exe + ' -file ' + self.input_file_name

        # The execution of the command `sbt "exe"`. The output file is generated.
        subprocess.Popen('sbt "{}"'.format(exe), shell=True).wait()

        # Read the output file
        result_text = ''
        with open(self.output_file_name, mode='r', encoding='utf-8') as f:
            result_text = f.read()

        # Remove the input file and the output file
        subprocess.Popen(['rm', self.input_file_name, self.output_file_name]).wait()

        self.assertTrue(elements_equal(ET.fromstring(result_text), ET.fromstring(expected_text)))

    def check_equal_with_java(self,
                              exe: str,
                              input_text: str,
                              expected_text: str):
        '''check the expected text and the result text are equal.
        This function uses java command.
        '''
        # The constants
        self.input_file_name = 'input_.txt'
        self.output_file_name = self.input_file_name + '.xml'

        # Write to the input file `input_.txt`
        with open(self.input_file_name, mode='w', encoding='utf-8') as f:
            f.write(input_text)

        # Add the command option
        exe = exe + ' -file ' + self.input_file_name

        # The execution of the command `sbt "exe"`. The output file is generated.
        subprocess.Popen('{}'.format(exe), shell=True).wait()

        # Read the output file
        result_text = ''
        with open(self.output_file_name, mode='r', encoding='utf-8') as f:
            result_text = f.read()

        # Remove the input file and the output file
        subprocess.Popen(['rm', self.input_file_name, self.output_file_name]).wait()

        self.assertTrue(elements_equal(ET.fromstring(result_text), ET.fromstring(expected_text)))

    def check_any(self):
        '''you can add any function.
        '''
        return None
