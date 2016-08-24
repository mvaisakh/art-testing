/*
 * Copyright (C) 2016 Linaro Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package benchmarks.algorithm;

class Record {
  Record ptrComp;
  int discr;
  int enumComp;
  int intComp;
  StringBuilder strComp;
  int enumComp2;
  char ch1Comp;
  char ch2Comp;
}

class Pointer<T> {
  T ref;
}

public class Dhrystone {
  Record ptrGlob;
  Record nextPtrGlob;
  int intGlob;
  boolean boolGlob;
  char ch1Glob;
  char ch2Glob;
  int []arr1Glob;
  int [][]arr2Glob;
  int numOfRuns;

  public static final int Ident_1 = 0;
  public static final int Ident_2 = 1;
  public static final int Ident_3 = 2;
  public static final int Ident_4 = 3;
  public static final int Ident_5 = 4;

  public Dhrystone() {
    ptrGlob = new Record();
    nextPtrGlob = new Record();

    ptrGlob.ptrComp = nextPtrGlob;
    ptrGlob.discr = Ident_1;
    ptrGlob.enumComp = Ident_3;
    ptrGlob.intComp = 40;
    ptrGlob.strComp = new StringBuilder(30);

    nextPtrGlob.strComp = new StringBuilder(30);

    arr1Glob = new int[50];
    arr2Glob = new int[50][50];
    arr2Glob[8][7] = 10;
  }

  public void timeDhrystone(int iterations) {
    int runIndex;
    int int1Loc;
    int int2Loc;
    int int3Loc = 0;
    Pointer<Integer> intRef = new Pointer<Integer>();
    String str1Loc = "DHRYSTONE PROGRAM, 1'ST STRING";
    StringBuilder str2Loc = new StringBuilder(30);

    int enumLoc = 0;
    char chIndex;

    /* We should re-init the parameters on every test */
    ptrGlob.ptrComp = nextPtrGlob;
    ptrGlob.discr = Ident_1;
    ptrGlob.enumComp = Ident_3;
    ptrGlob.intComp = 40;
    ptrGlob.strComp.setLength(0);
    ptrGlob.strComp.append("DHRYSTONE PROGRAM, SOME STRING");
    arr2Glob[8][7] = 10;
    numOfRuns = iterations;

    for (runIndex = 1; runIndex <= iterations; runIndex++) {
      proc5();
      proc4();

      int1Loc = 2;
      int2Loc = 3;
      str2Loc.setLength(0);
      str2Loc.append("DHRYSTONE PROGRAM, 2'ND STRING");
      enumLoc = Ident_2;

      /* boolGlob == 1 */
      boolGlob = !func2(str1Loc, str2Loc.toString());

      while (int1Loc < int2Loc) {  /* loop body executed once */
        /* int3Loc == 7 */
        int3Loc = 5 * int1Loc - int2Loc;

        /* int3Loc == 7 */
        intRef.ref = int3Loc;
        proc7(int1Loc, int2Loc, intRef);
        int3Loc = intRef.ref;

        int1Loc += 1;
      }

      /* int1Loc == 3, int2Loc == 3, int3Loc == 7 */
      proc8(arr1Glob, arr2Glob, int1Loc, int3Loc);

      /* intGlob == 5 */
      proc1(ptrGlob);

      for (chIndex = 'A'; chIndex <= ch2Glob; ++chIndex) {
        /* loop body executed twice */
        if (enumLoc == func1(chIndex, 'C')) {
          /* then, not executed */
          intRef.ref = enumLoc;
          proc6(Ident_1, intRef);
          enumLoc = intRef.ref;

          str2Loc.setLength(0);
          str2Loc.append("DHRYSTONE PROGRAM, 3'RD STRING");
          int2Loc = runIndex;
          intGlob = runIndex;
        }
      }

      /* int1Loc == 3, int2Loc == 3, int3Loc == 7 */
      int2Loc = int2Loc * int1Loc;
      int1Loc = int2Loc / int3Loc;
      int2Loc = 7 * (int2Loc - int3Loc) - int1Loc;
      /* int1Loc == 1, int2Loc == 13, int3Loc == 7 */

      intRef.ref = int1Loc;
      proc2(intRef);
      int1Loc = intRef.ref;

      /* int1Loc == 5 */
    }
  }

  public boolean verifyDhrystone() {
    String str = "DHRYSTONE PROGRAM, SOME STRING";

    timeDhrystone(1);

    if (intGlob != 5) {
      System.out.println("ERROR: intGlob should be 5, but found " + intGlob);
      return false;
    }

    if (boolGlob != true) {
      System.out.println("ERROR: boolGlob should be true, but found " + boolGlob);
      return false;
    }

    if (ch1Glob != 'A') {
      System.out.println("ERROR: ch1Glob should be 'A', but found " + ch1Glob);
      return false;
    }

    if (ch2Glob != 'B') {
      System.out.println("ERROR: ch2Glob should be 'B', but found " + ch2Glob);
      return false;
    }

    if (arr1Glob[8] != 7) {
      System.out.println("ERROR: arr1Glob[8] should be 7, but found " + arr1Glob[8]);
      return false;
    }

    if (arr2Glob[8][7] != (numOfRuns + 10)) {
      System.out.println("ERROR: arr2Glob[8][7] should be " + (numOfRuns + 10) +
                         ", but found " + arr2Glob[8][7]);
      return false;
    }

    if (ptrGlob.discr != 0) {
      System.out.println("ERROR: ptrGlob.discr should be 0, but found " + ptrGlob.discr);
      return false;
    }

    if (ptrGlob.enumComp != 2) {
      System.out.println("ERROR: ptrGlob.enumComp should be 2, but found " + ptrGlob.enumComp);
      return false;
    }

    if (ptrGlob.intComp != 17) {
      System.out.println("ERROR: ptrGlob.intComp should be 17, but found " + ptrGlob.intComp);
      return false;
    }

    if (ptrGlob.strComp.toString().compareTo(str) != 0) {
      System.out.println("ERROR: ptrGlob.strComp should be " + str + ", but found " +
                          ptrGlob.strComp);
      return false;
    }

    if (nextPtrGlob.discr != 0) {
      System.out.println("ERROR: nextPtrGlob.discr should be 0, but found " +
                          nextPtrGlob.discr);
      return false;
    }

    if (nextPtrGlob.enumComp != 1) {
      System.out.println("ERROR: nextPtrGlob.enumComp should be 1, but found " +
                         nextPtrGlob.enumComp);
      return false;
    }

    if (nextPtrGlob.intComp != 18) {
      System.out.println("ERROR: nextPtrGlob.intComp should be 18, but found " +
                          nextPtrGlob.intComp);
      return false;
    }

    if (nextPtrGlob.strComp.toString().compareTo(str) != 0) {
      System.out.println("ERROR: nextPtrGlob.strComp should be " + str + ", but found " +
                         nextPtrGlob.strComp);
      return false;
    }

    return true;
  }

  /* executed once */
  public void proc1(Record ptrValPar) {
    /* == ptrGlob_Next */
    /* Local variable, initialized with ptrValPar->ptrComp,    */
    /* corresponds to "rename" in Ada, "with" in Pascal        */
    Record nextRecord = ptrValPar.ptrComp;

    ptrValPar.ptrComp.ptrComp = ptrGlob.ptrComp;
    ptrValPar.ptrComp.discr = ptrGlob.discr;
    ptrValPar.ptrComp.enumComp = ptrGlob.enumComp;
    ptrValPar.ptrComp.intComp = ptrGlob.intComp;
    ptrValPar.ptrComp.strComp.setLength(0);
    ptrValPar.ptrComp.strComp.append(ptrGlob.strComp.toString());
    ptrValPar.ptrComp.enumComp2 = ptrGlob.enumComp2;
    ptrValPar.ptrComp.ch1Comp = ptrGlob.ch1Comp;
    ptrValPar.ptrComp.ch2Comp = ptrGlob.ch2Comp;
    ptrValPar.intComp = 5;

    nextRecord.intComp = ptrValPar.intComp;
    nextRecord.ptrComp = ptrValPar.ptrComp;

    proc3(nextRecord.ptrComp);

    if (nextRecord.discr == Ident_1) {
      /* then, executed */
      nextRecord.intComp = 6;

      Pointer<Integer> intRef = new Pointer<Integer>();

      intRef.ref = nextRecord.enumComp;
      proc6(ptrValPar.enumComp, intRef);
      nextRecord.enumComp = intRef.ref;

      nextRecord.ptrComp = ptrGlob.ptrComp;

      intRef.ref = nextRecord.intComp;
      proc7(nextRecord.intComp, 10, intRef);
      nextRecord.intComp = intRef.ref;
    }
    else { /* not executed */
      ptrValPar = ptrValPar.ptrComp;
    }
  }

  /* executed once */
  /* intParRef == 1, becomes 4 */
  public void proc2(Pointer<Integer> intParRef) {
    int intLoc;
    int enumLoc = 0;

    intLoc = intParRef.ref + 10;
    do { /* executed once */
      if (ch1Glob == 'A') {
        /* then, executed */
        intLoc -= 1;
        intParRef.ref = intLoc - intGlob;
        enumLoc = Ident_1;
      }
    }
    while (enumLoc != Ident_1);
  }

  /* executed once */
  /* ptrRefPar becomes ptrGlob */
  public void proc3(Record ptrRefPar) {
    if (ptrGlob != null) {
      /* then, executed */
      ptrRefPar = ptrGlob.ptrComp;
    }

    Pointer<Integer> intRef = new Pointer<Integer>();
    intRef.ref = ptrGlob.intComp;
    proc7(10, intGlob, intRef);
    ptrGlob.intComp = intRef.ref;
  }

  public void proc4() {
    Boolean boolLoc;
    boolLoc = ch1Glob == 'A';
    boolGlob = boolLoc | boolGlob;
    ch2Glob = 'B';
  }

  public void proc5() {
    ch1Glob = 'A';
    boolGlob = false;
  }

  /* executed once */
  /* enumValPar == Ident_3, enumRefPar becomes Ident_2 */
  public void proc6(int enumValPar, Pointer<Integer> enumRefPar) {
    enumRefPar.ref = enumValPar;
    if (!func3(enumValPar)) {
      /* then, not executed */
      enumRefPar.ref = Ident_4;
    }

    switch (enumValPar) {
      case Ident_1:
        enumRefPar.ref = Ident_1;
        break;
      case Ident_2:
        if (intGlob > 100) {
          /* then */
          enumRefPar.ref = Ident_1;
        }
        else {
          enumRefPar.ref = Ident_4;
        }
        break;
      case Ident_3: /* executed */
        enumRefPar.ref = Ident_2;
        break;
      case Ident_4:
        break;
      case Ident_5:
        enumRefPar.ref = Ident_3;
        break;
      default:
        break;
    }
  }

  /* executed three times                                */
  /* first call:      int1ParVal == 2, int2ParVal == 3,  */
  /*                  intParRef becomes 7                */
  /* second call:     int1ParVal == 10, int2ParVal == 5, */
  /*                  intParRef becomes 17               */
  /* third call:      int1ParVal == 6, int2ParVal == 10, */
  /*                  intParRef becomes 18               */
  public void proc7(int int1ParVal, int int2ParVal, Pointer<Integer> intParRef) {
    int intLoc;

    intLoc = int1ParVal + 2;
    intParRef.ref = int2ParVal + intLoc;
  }

  /* executed once      */
  /* Int_Par_Val_1 == 3 */
  /* Int_Par_Val_2 == 7 */
  public void proc8(int []arr1ParRef, int [][]arr2ParRef, int int1ParVal, int int2ParVal) {
    int intIndex;
    int intLoc;

    intLoc = int1ParVal + 5;
    arr1ParRef[intLoc] = int2ParVal;
    arr1ParRef[intLoc + 1] = arr1ParRef[intLoc];
    arr1ParRef[intLoc + 30] = intLoc;

    for (intIndex = intLoc; intIndex <= intLoc + 1; ++intIndex) {
      arr2ParRef[intLoc][intIndex] = intLoc;
    }

    arr2ParRef[intLoc][intLoc - 1] += 1;
    arr2ParRef[intLoc + 20][intLoc] = arr1ParRef[intLoc];
    intGlob = 5;
  }

  public int func1(char ch1ParVal, char ch2ParVal) {
    char ch1Loc;
    char ch2Loc;

    ch1Loc = ch1ParVal;
    ch2Loc = ch1Loc;
    if (ch2Loc != ch2ParVal) {
      /* then, executed */
      return Ident_1;
    }
    else { /* not executed */
      ch1Glob = ch1Loc;
      return Ident_2;
    }
  }

  /* executed once */
  /* str1ParRef == "DHRYSTONE PROGRAM, 1'ST STRING" */
  /* str2ParRef == "DHRYSTONE PROGRAM, 2'ND STRING" */
  public boolean func2(String str1ParRef, String str2ParRef) {
    int intLoc;
    char chLoc = 0;

    intLoc = 2;
    while (intLoc <= 2) /* loop body executed once */
      if (func1(str1ParRef.charAt(intLoc), str2ParRef.charAt(intLoc + 1)) == Ident_1) {
        /* then, executed */
        chLoc = 'A';
        intLoc += 1;
      }

    if (chLoc >= 'W' && chLoc < 'Z') {
      /* then, not executed */
      intLoc = 7;
    }

    if (chLoc == 'R') {
      /* then, not executed */
      return true;
    }
    else { /* executed */
      if (str1ParRef.compareTo(str2ParRef) > 0) {
        /* then, not executed */
        intLoc += 7;
        intGlob = intLoc;
        return true;
      }
      else { /* executed */
        return false;
      }
    }
  }

  /* executed once         */
  /* enumParVal == Ident_3 */
  public boolean func3(int enumParVal) {
    int enumLoc;

    enumLoc = enumParVal;
    if (enumLoc == Ident_3) {
      /* then, executed */
      return true;
    }
    else { /* not executed */
      return false;
    }
  }

  public static void main(String []argv) {
    int rc = 0;
    Dhrystone obj = new Dhrystone();

    long before = System.currentTimeMillis();
    obj.timeDhrystone(500000);
    long after = System.currentTimeMillis();

    System.out.println("benchmarks/algorithm/Dhrystone: " + (after - before));

    if (!obj.verifyDhrystone()) {
      rc++;
    }

    System.exit(rc);
  }
}
