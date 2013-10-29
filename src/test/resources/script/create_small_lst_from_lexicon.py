#!/usr/bin/env python

''' This script is used for creating data used for test

Output is already included in resources/data directory
as `template.small.lst`, so usually this file is unnecessary.

Example usage from the project root directory is
./src/test/resources/script/create_small_lst_from_lexicon.py \
./ccgbank/template.lst

'''

import sys, os

if __name__ == '__main__':
    if len(sys.argv) < 2:
        print "usage", sys.argv[0], "full_template_lst"
        exit()

    data_dir = os.path.abspath(os.path.dirname(__file__))+'/../data'
    small_lexicon_path = data_dir+'/Japanese.small.lexicon'
    output_path = data_dir+'/template.small.lst'
    
    cat_tmps = []
    for line in open(small_lexicon_path):
        cat_tmps += line.strip().split(' ')[1:]
    cat_tmps = set(cat_tmps)

    with open(output_path, 'w') as f:
        for line in open(sys.argv[1]):
            line = line.strip().split('\t')
            cat_tmp = line[0]
            cat_str = line[1]

            if cat_tmp in cat_tmps:
                f.write("%s\t%s\n" % (cat_tmp, cat_str))
            
