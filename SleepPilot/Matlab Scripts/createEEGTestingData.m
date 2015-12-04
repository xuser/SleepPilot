% X ist die Matrix mit den Daten und from und to beschreibt den Bereich aus
% dem die Daten aus der Matrix entnommen werden sollen.

%fid = fopen('magic5.txt', 'w');
%fwrite(fid, M, 'float32');
%fclose(fid);

function F = createEEGTestingData(X,from,to)

dimX = size(X);
zeilenX = dimX(1);
spaltenX = dimX(2);

spalten = to - from;
spalten = spalten + 1;

F = zeros(zeilenX,spalten);

for i=from:to
    for y=1:zeilenX
        F((y),(i-from+1)) = X(y,i);
    end
end

fid = fopen('testDatenDominic2011_10000to12000.txt', 'w');
fwrite(fid, F, 'float32');
fclose(fid);