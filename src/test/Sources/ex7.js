function A() {}

A.prototype./*FIND:*/foo = function() {};
A.prototype.bar = function() {};

function B() {}
B.prototype = Object.create(A.prototype);
B.prototype.foo = function() {};

function C() {}
C.prototype = Object.create(A.prototype);
C.prototype.bar = function() {};

function E() {}
E.prototype = Object.create(C.prototype);

function F() {}
F.prototype = Object.create(C.prototype);
F.prototype.foo = function() {};


function D() {}
D.prototype = C.prototype;

function test1(a) {
    a.foo() // potential usage

    if (a instanceof B) {
        test2(a)
    }
    if (a instanceof A) {

        if (a instanceof E) {
          a.bar();
          a.foo();
        } else
            if (a instanceof F)
              a.foo();


        a.bar();

        a.bar()
    }
}

function test2(a) {
 if (a instanceof A) {
   a.bar();
   if (a instanceof B) {
     a.foo();
   } else {
     if (a instanceof C) {
       a.foo();
       if (a instanceof E) {
         a.bar();
       } else {
         if (a instanceof F) {
           a.bar();
         } else {
           a.foo();
         }
       }
     }
   }
 }
}