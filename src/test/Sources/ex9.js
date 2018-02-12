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


function test(a,b,c,d,e,g,f) {
  while (f instanceof C) {
  //we have to copy current state
    if (a instanceof A) {
      if (a == d) {
        d.foo();
      } else {
        c.foo();
        a.foo();
      }
    } else {
      if (b instanceof B) {
        if (e instanceof C) {
          if (e == g) {
           g.foo();
          } else {
            f.foo();
            if (b == e) {
              e.foo();
            }
            b = new C;
          }
        }
        b.foo();
      }
    }
  }
}


function test2(a, b, c) {

  if (a == null) {
    a = new A;
    a.foo();
  }

  a.foo();

  if (!(b instanceof A)) {
    a.foo();
  } else {
      while (a == b) {
        a.foo();
        a = null;
        a.foo();
      }
  }

  if (b instanceof A) {
    b.foo();
  }

}