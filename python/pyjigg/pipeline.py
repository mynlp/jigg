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

    def annotate(self, text, properties=None):
        assert isinstance(text, str)
        if properties is None:
            properties = {}
        else:
            assert isinstance(properties, dict)

        # Checks that the Jigg Pipeline server is started.
        try:
            requests.get(self.server_url)
        except requests.exceptions.ConnectionError:
            raise Exception('Check whether you have started the Jigg\'s PipelineServer e.g.\n'
                            '$ cd %s/ \n'
                            '$ java -Xmx4g -cp "*" jigg.pipeline.PipelineServer' % (JIGG))

        url = self.server_url + '/annotate'
        text = text.encode()
        data = properties.copy()
        data['q'] = text
        r = requests.post(url, data=data)
        output = r.text
        if ('outputFormat' in properties and properties['outputFormat'] == 'json'):
            try:
                output = json.loads(output, encoding='utf-8', strict=True)
            except:
                pass
        else:
            try:
                output = ET.fromstring(output)
            except:
                pass

        return output
