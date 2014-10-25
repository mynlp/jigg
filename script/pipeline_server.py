#!/usr/bin/env python
# -*- coding: utf-8 -*-

import optparse
import time
import logging
import pexpect
from SimpleXMLRPCServer import SimpleXMLRPCServer

logging.basicConfig(level=logging.DEBUG)
logger = logging.getLogger(__name__)

class Pipeline(object):
    def __init__(self, args):
        cmd = "java %s" % args
        logger.info(cmd)
        
        self.pipeline = pexpect.spawn(cmd)
        self.pipeline.expect("> ", timeout=500)
        logger.info("Model load done!")

    def parse(self, text):
        logger.info(text)
        while True:
            try:
                self.pipeline.read_nonblocking (4000, 0.3)
            except pexpect.TIMEOUT:
                break
        
        self.pipeline.sendline(text)

        # How much time should we give the parser to parse it?
        # the idea here is that you increase the timeout as a 
        # function of the text's length.
        # anything longer than 5 seconds requires that you also
        # increase timeout=5 in jsonrpc.py
        max_expected_time = min(40, 3 + len(text) / 20.0)
        end_time = time.time() + max_expected_time

        incoming = ""
        while True:
            try:
                incoming += self.pipeline.read_nonblocking(2000, 1)
                end = incoming.find("\n>")
                if end != -1:
                    incoming = incoming[:end]
                    break
                time.sleep(0.0001)
            except pexpect.TIMEOUT:
                if end_time - time.time() < 0:
                    logger.error("Error: Timeout with input '%s'" % (incoming))
                    return {'error': "timed out after %f seconds" % max_expected_time}
                else:
                    continue
            except pexpect.EOF:
                break
        
        logger.info("%s\n%s" % ('='*40, incoming))

        # try:
        #     results = parse_parser_results(incoming)
        # except Exception, e:
        #     if VERBOSE: 
        #         logger.debug(traceback.format_exc())
        #     raise e

        start = incoming.find("<sentences>")
        incoming = incoming[start:]
        return incoming

if __name__ == '__main__':
    """
    Example usage:

    ./script/pipeline_server.py  -P "-Xmx4g -cp transccg-0.2.jar enju.pipeline.Pipeline -annotators ssplit,kuromoji -numKbest 3"
    
    """
    
    parser = optparse.OptionParser(usage="%prog [OPTIONS]")
    parser.add_option('-p', '--port', default='8080',
                      help='Port to serve on (default: 8080)')
    parser.add_option('-H', '--host', default='127.0.0.1',
                      help='Host to serve on (default: 127.0.0.1. Use 0.0.0.0 to make public)')
    parser.add_option('-P', '--pipeline',
                      help='arguments required to run transccg pipeline.')
    options, args = parser.parse_args()

    server = SimpleXMLRPCServer((options.host, int(options.port)), logRequests=True)

    pipeline = Pipeline(options.pipeline)
    
    server.register_function(pipeline.parse)

    logger.info('Serving on http://%s:%s' % (options.host, options.port))
    server.serve_forever()
