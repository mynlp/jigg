#!/usr/bin/env python

import xml.etree.ElementTree as ET
import json
import requests

JIGG = 'jigg-0.6.2'

class Pipeline:

    def __init__(self, server_url):
        if server_url[-1] == '/':
            server_url = server_url[:-1]
        self.server_url = server_url

    def annotateJSON(self, text):
        output = self._annotate(text, 'json')
        return json.loads(output, encoding='utf-8', strict=True)

    def annotateXML(self, text):
        output = self._annotate(text, 'xml')
        return ET.fromstring(output)

    def _annotate(self, text, format):
        assert isinstance(text, str)

        # Checks that the server is started.
        try:
            requests.get(self.server_url)
        except requests.exceptions.ConnectionError:
            raise Exception('Check whether you have started the Jigg\'s PipelineServer e.g.\n'
                            '$ cd %s/ \n'
                            '$ java -Xmx4g -cp "*" jigg.pipeline.PipelineServer' % (JIGG))

        url = self.server_url + '/annotate'

        data = text.encode()
        r = requests.post(url, params={'format': format}, data=data)
        return r.text
        # output = r.text
