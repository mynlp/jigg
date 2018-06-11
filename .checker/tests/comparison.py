import xml.etree.ElementTree as ET
from xml.etree.ElementTree import Element


def elements_equal(e1: Element, e2: Element) -> bool:
    ''' check equivalence of two xml elements.
    '''
    if _compare_tag(e1, e2):
        pass
    else:
        return False

    if _compare_text(e1, e2):
        pass
    else:
        return False

    if _compare_tail(e1, e2):
        pass
    else:
        return False

    if _compare_attributes(e1, e2):
        pass
    else:
        return False

    if _compare_length(e1, e2):
        pass
    else:
        return False

    return all(elements_equal(c1, c2) for c1, c2 in zip(e1, e2))


def _compare_tag(e1: Element, e2: Element) -> bool:
    tag1 = e1.tag
    tag2 = e2.tag
    return True if tag1 == tag2 else False


def _compare_text(e1: Element, e2: Element) -> bool:
    # remove whitespace in text.
    text1 = e1.text if e1.text == None else e1.text.strip()
    text2 = e2.text if e2.text == None else e2.text.strip()
    return True if text1 == text2 else False


def _compare_tail(e1: Element, e2: Element) -> bool:
    # remove whitespae in tail
    tail1 = e1.tail if e1.tail is None else e1.tail.strip()
    tail2 = e2.tail if e2.tail is None else e2.tail.strip()
    return True if tail1 == tail2 else False


def _compare_attributes(e1: Element, e2: Element) -> bool:
    attrib1 = e1.attrib
    attrib2 = e2.attrib
    return True if attrib1 == attrib2 else False


def _compare_length(e1: Element, e2: Element) -> bool:
    length1 = len(e1)
    length2 = len(e2)
    return True if length1 == length2 else False
