
#ifndef SORT_C
#define SORT_C

#include <queue>
#include "MyDB_PageReaderWriter.h"
#include "MyDB_TableRecIterator.h"
#include "MyDB_TableRecIteratorAlt.h"
#include "MyDB_TableReaderWriter.h"
#include "IteratorComparator.h"
#include "Sorting.h"
#include "../headers/MyDB_PageReaderWriter.h"

using namespace std;

void mergeIntoFile (MyDB_TableReaderWriter &sortIntoMe, vector <MyDB_RecordIteratorAltPtr> &mergeUs, 
	function <bool ()> comparator, MyDB_RecordPtr lhs, MyDB_RecordPtr rhs) {

	// create the comparator and the priority queue
	IteratorComparator temp (comparator, lhs, rhs);
	priority_queue <MyDB_RecordIteratorAltPtr, vector <MyDB_RecordIteratorAltPtr>, IteratorComparator> pq (temp);

	// load up the set
	for (MyDB_RecordIteratorAltPtr m : mergeUs) {
		if (m->advance ()) {
			pq.push (m);
		}
	}

	// and write everyone out
	int counter = 0;
	while (pq.size () != 0) {

		// write the dude to the output
		auto myIter = pq.top ();
		myIter->getCurrent (lhs);
		sortIntoMe.append (lhs);
		counter++;

		// remove from the q
		pq.pop ();

		// re-insert
		if (myIter->advance ()) {
			pq.push (myIter);
		}
	}
}

void appendRecord (MyDB_PageReaderWriter &curPage, vector <MyDB_PageReaderWriter> &returnVal, 
	MyDB_RecordPtr appendMe, MyDB_BufferManagerPtr parent) {

	// try to append to the current page
	if (!curPage.append (appendMe)) {

		// if we cannot, then add a new one to the output vector
		returnVal.push_back (curPage);
		MyDB_PageReaderWriter temp (*parent);
		temp.append (appendMe);
		curPage = temp;
	}
}

vector <MyDB_PageReaderWriter> mergeIntoList (MyDB_BufferManagerPtr parent, MyDB_RecordIteratorAltPtr leftIter, 
	MyDB_RecordIteratorAltPtr rightIter, function <bool ()> comparator, MyDB_RecordPtr lhs, MyDB_RecordPtr rhs) {
	
	vector <MyDB_PageReaderWriter> returnVal;
	MyDB_PageReaderWriter curPage (*parent);
	bool lhsLoaded = false, rhsLoaded = false;

	// if one of the runs is empty, get outta here
	if (!leftIter->advance ()) {
		while (rightIter->advance ()) {
			rightIter->getCurrent (rhs);
			appendRecord (curPage, returnVal, rhs, parent);
		}
	} else if (!rightIter->advance ()) {
		while (leftIter->advance ()) {
			leftIter->getCurrent (lhs);
			appendRecord (curPage, returnVal, lhs, parent);
		}
	} else {
		while (true) {
	
			// get the two records

			// here's a bit of an optimization... if one of the records is loaded, don't re-load
			if (!lhsLoaded) {
				leftIter->getCurrent (lhs);
				lhsLoaded = true;
			}

			if (!rhsLoaded) {
				rightIter->getCurrent (rhs);		
				rhsLoaded = true;
			}
	
			// see if the lhs is less
			if (comparator ()) {
				appendRecord (curPage, returnVal, lhs, parent);
				lhsLoaded = false;

				// deal with the case where we have to append all of the right records to the output
				if (!leftIter->advance ()) {
					appendRecord (curPage, returnVal, rhs, parent);
					while (rightIter->advance ()) {
						rightIter->getCurrent (rhs);
						appendRecord (curPage, returnVal, rhs, parent);
					}
					break;
				}
			} else {
				appendRecord (curPage, returnVal, rhs, parent);
				rhsLoaded = false;

				// deal with the ase where we have to append all of the right records to the output
				if (!rightIter->advance ()) {
					appendRecord (curPage, returnVal, lhs, parent);
					while (leftIter->advance ()) {
						leftIter->getCurrent (lhs);
						appendRecord (curPage, returnVal, lhs, parent);
					}
					break;
				}
			}
		}
	}
	
	// remember the current page
	returnVal.push_back (curPage);
	
	// outta here!
	return returnVal;
}
	
void sort(int runSize, MyDB_TableReaderWriter &sortMe, MyDB_TableReaderWriter &sortIntoMe,
		function <bool ()> comparator, MyDB_RecordPtr lhs, MyDB_RecordPtr rhs) {
	int pageNum = 0;
	int curRunSize = runSize;
	std::vector<MyDB_RecordIteratorAltPtr> allRuns;
	std::vector<MyDB_PageReaderWriter> run;
	//TODO something is being done inefficiently!
	while (pageNum < sortMe.getNumPages()) { //make sure every page is inluded in some run
		if (curRunSize == runSize) { //the run is done, so add it, and reset the run
			MyDB_RecordIteratorAltPtr ptr = getIteratorAlt(run);
			allRuns.push_back(ptr); //add the run to the vector of all runs
			curRunSize = 0;
			std::vector<MyDB_PageReaderWriter> temp;
			run = temp; //reset
		}
		MyDB_PageReaderWriterPtr sortedPage = sortMe[pageNum++].sort(comparator, lhs, rhs); //sort the page
		run = mergeIntoList(sortMe.getBufferMgr(), sortedPage->getIteratorAlt(), getIteratorAlt(run),
				comparator, lhs, rhs); //now merge this sorted page into our run
		curRunSize++;
	}
	mergeIntoFile(sortIntoMe, allRuns, comparator, lhs, rhs);
}

#endif
