e(X,Y, Z) :- X = 9223372036854775807, Y is X+Z.
e(X,Y, Z) :- X = -9223372036854775808, Y is X+Z.

%QUERY e(X, Y, 1)
%ANSWER
% X = 9223372036854775807
% Y = -9223372036854775808
%ANSWER
%ANSWER
% X = -9223372036854775808
% Y = -9223372036854775807
%ANSWER

%QUERY e(X, Y, -1)
%ANSWER
% X = 9223372036854775807
% Y = 9223372036854775806
%ANSWER
%ANSWER
% X = -9223372036854775808
% Y = 9223372036854775807
%ANSWER