p(X,Y,Z).

%TRUE p(a,a,a)
%TRUE p(b,a,a)
%TRUE p(a,b,a)
%TRUE p(a,a,b)
%TRUE p(a,b,c)
%TRUE p(x,y,z)

%QUERY p(A,B,C)
%ANSWER
% A=UNINSTANTIATED VARIABLE
% B=UNINSTANTIATED VARIABLE
% C=UNINSTANTIATED VARIABLE
%ANSWER

%QUERY D=x(A,B,C), p(A,B,C)
%ANSWER
% A=UNINSTANTIATED VARIABLE
% B=UNINSTANTIATED VARIABLE
% C=UNINSTANTIATED VARIABLE
% D=x(A, B, C)
%ANSWER
