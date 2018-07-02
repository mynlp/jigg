from xml.etree.ElementTree import Element


def elements_equal(e1: Element, e2: Element) -> bool:
    ''' check equivalence of two xml elements.
    '''
    if _compare_tag(e1, e2):
        pass
    else:
        print(e1.tag, e2.tag)
        return False

    if _compare_text(e1, e2):
        pass
    else:
        print(e1.text, e2.text)
        return False

    if _compare_tail(e1, e2):
        pass
    else:
        print(e1.tail, e2.tail)
        return False

    if _compare_attributes(e1, e2):
        pass
    else:
        print(e1.attrib, e2.attrib)
        return False

    if _compare_length(e1, e2):
        pass
    else:
        print(len(e1), len(e2))
        return False

    return all(elements_equal(c1, c2) for c1, c2 in zip(e1, e2))


def _compare_tag(e1: Element, e2: Element) -> bool:
    """
    >>> import xml.etree.ElementTree as ET
    >>> _compare_tag(ET.fromstring("<a> </a>"), ET.fromstring("<a> </a>"))
    True
    >>> _compare_tag(ET.fromstring("<a> </a>"), ET.fromstring("<b> </b>"))
    False
    """
    tag1 = e1.tag
    tag2 = e2.tag
    return True if tag1 == tag2 else False


def _compare_text(e1: Element, e2: Element) -> bool:
    """
    >>> import xml.etree.ElementTree as ET
    >>> _compare_text(ET.fromstring("<a> text1 </a>"), ET.fromstring("<a> text1 </a>"))
    True
    >>> _compare_text(ET.fromstring("<a> text1 </a>"), ET.fromstring("<a> text2 </a>"))
    False
    """
    # remove whitespace in text.
    text1 = e1.text if e1.text is None else e1.text.strip()
    text2 = e2.text if e2.text is None else e2.text.strip()
    return True if text1 == text2 else False


def _compare_tail(e1: Element, e2: Element) -> bool:
    """
    >>> import xml.etree.ElementTree as ET
    >>> p1 = ET.fromstring("<a><b></b>tail1</a>")
    >>> p2 = ET.fromstring("<a><b></b> tail1</a>")
    >>> p3 = ET.fromstring("<a><b></b>tail2</a>")
    >>> _compare_tail(p1.find('b'), p2.find('b'))
    True
    >>> _compare_tail(p1.find('b'), p3.find('b'))
    False
    """
    # remove whitespae in tail
    tail1 = e1.tail if e1.tail is None else e1.tail.strip()
    tail2 = e2.tail if e2.tail is None else e2.tail.strip()
    return True if tail1 == tail2 else False


def _compare_attributes(e1: Element, e2: Element) -> bool:
    """
    >>> import xml.etree.ElementTree as ET
    >>> a1 = ET.fromstring("<a id='1' form='word' />")
    >>> a2 = ET.fromstring("<a form='word' id='1' />")
    >>> a3 = ET.fromstring("<a id='2' form='word' />")
    >>> _compare_attributes(a1, a2)
    True
    >>> _compare_attributes(a1, a3)
    False
    """
    attrib1 = e1.attrib
    attrib2 = e2.attrib
    return True if attrib1 == attrib2 else False


def _compare_length(e1: Element, e2: Element) -> bool:
    """
    >>> import xml.etree.ElementTree as ET
    >>> _compare_length(ET.fromstring("<a><b/></a>"), ET.fromstring("<a><b/></a>"))
    True
    >>> _compare_length(ET.fromstring("<a><b/></a>"), ET.fromstring("<a></a>"))
    False
    """
    length1 = len(e1)
    length2 = len(e2)
    return True if length1 == length2 else False
