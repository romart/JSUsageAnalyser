

function A() {}

A.prototype.foo = function() {}
A.prototype.bar = function(a) {
  this.foo(); // exact
  foo(); // exact
}

function test1(a) {

  var b = fafafa();

  test2(a);
  a.foo(); // exact
  a.foobar.foo(); // possible usage

  test1(test2(a));

  return new A;
}

function test2(a) {
   a.foo(); // exact

   test2(test1(a));

   return new A;
}

test1(new A)