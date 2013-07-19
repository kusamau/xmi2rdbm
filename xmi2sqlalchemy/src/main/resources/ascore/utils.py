'''
Created on 6 Feb 2012

@author: mnagni
'''
def has_value(item):
    if item is not None:
        if (isinstance(item, list) and len(item) > 0) or not isinstance(item, list):            
            return True
        else:
            return False
    return False

def collectionProofHash(tupleToHash):
    return hash(tuple(_convertCollectionToList(tupleToHash)))

def _convertCollectionToList(tupleToHash, processedItems = []): 
    toHash = []
    for item in tupleToHash:
        if item in processedItems:
            continue
        processedItems.append(item)
        if type(item) == tuple or type(item) == list:
            for innerItem in item:
                toHash.extend(collectionProofHash(innerItem, processedItems))
        elif type(item) == dict:
            for innerItem in item.keys():
                toHash.extend(collectionProofHash(innerItem, processedItems))
            for innerItem in item.values():
                toHash.extend(collectionProofHash(innerItem, processedItems))
        else:
            toHash.append(item)
    return toHash