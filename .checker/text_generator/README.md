## Generate text files which is the result of stanfor-corenlp for each annotation.

On root directory of repository, you run the following script

```
./.checker/text_generator/generate-all.sh
```

This script executes the stanford-corenlp and jigg for each annotation.
The types of annotation are as follows:

- tokenize
- ssplit
- pos
- lemma
- ner
- parse
- dcoref  

The output files are generated in the derectories
`.checker/stanford_corenlp/text/` or `.checker/jigg/text/`.
The rule of file name is `{type of annotation}-target.txt`.
For example, the type of annotation is `tokenize`,
the file name is `tokenize-target.txt`.
