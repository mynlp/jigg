
import argparse
from lxml import etree
import sys

from depccg.__main__ import add_common_parser_arguments
from depccg.combinator import en_default_binary_rules, ja_default_binary_rules
from depccg.download import load_model_directory, CONFIGS
from depccg.parser import EnglishCCGParser, JapaneseCCGParser
from depccg.printer import to_xml
from depccg.token import Token, english_annotator, japanese_annotator, annotate_XX
# from depccg.tokens import annotate_XX

Parsers = {'en': EnglishCCGParser, 'ja': JapaneseCCGParser}

"""An interface depccg called from DepCCGAnnotator.scala

Usage: python depccg.py nbest lang gpu model
"""

def print_xml(trees, tagged_doc):
    print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
    print("<?xml-stylesheet type=\"text/xsl\" href=\"candc.xml\"?>")
    print("<candc>")
    for i, (kbest, tokens) in enumerate(zip(trees, tagged_doc), 1):
        print("<ccgs sentence=\"{}\">".format(i))
        for j, (t, _) in enumerate(kbest, 1):
            ccg = t.xml(tokens)
            ccg.set("sentence", str(i))
            ccg.set("id", str(j))
            print(etree.tostring(ccg, encoding='utf-8', pretty_print=True).decode('utf-8'))
        print("</ccgs>")
    print("</candc>")

def build_parser(args):
    if args.lang == 'en':
        binary_rules = en_default_binary_rules
    elif args.lang == 'ja':
        binary_rules = ja_default_binary_rules

    if args.root_cats is not None:
        args.root_cats = args.root_cats.split(',')

    kwargs = dict(
        unary_penalty=args.unary_penalty,
        nbest=args.nbest,
        binary_rules=binary_rules,
        possible_root_cats=args.root_cats,
        pruning_size=args.pruning_size,
        beta=args.beta,
        use_beta=not args.disable_beta,
        use_seen_rules=not args.disable_seen_rules,
        use_category_dict=not args.disable_category_dictionary,
        max_length=args.max_length,
        max_steps=args.max_steps,
        gpu=args.gpu
    )

    use_allennlp = args.model and args.model.endswith('.tar.gz')
    config = args.config or CONFIGS[args.lang]
    if use_allennlp:
        parser = Parsers[args.lang].from_json(config, args.model, **kwargs)
    else:
        model = args.model or load_model_directory(args.lang)
        parser = Parsers[args.lang].from_json(config, model, **kwargs)

    return parser

def input_and_parse(parser):
    annotate_fun = annotate_XX
    try:
        while True:
            doc = []
            while True:
                line = input()
                if line == "####EOD####": break
                doc.append(line)

            tagged_doc = annotate_fun([[word for word in sent.split(' ')]
                                       for sent in doc],
                                      tokenize=None)
            parses = parser.parse_doc(doc)
            print_xml(parses, tagged_doc)
            print("END")
    except EOFError:
        pass

if __name__ == '__main__':
    parser = argparse.ArgumentParser('depccg python wrapper in Jigg')

    parser.add_argument('--lang', default='en', choices=['ja', 'en'])
    parser.add_argument('--internal-model', default=None)
    parser.add_argument('--internal-nbest', default=1, type=int)

    add_common_parser_arguments(parser)

    args = parser.parse_args()

    args.model = args.internal_model or args.model
    args.nbest = args.internal_nbest if args.internal_nbest != 1 else args.nbest

    parser = build_parser(args)

    input_and_parse(parser)
