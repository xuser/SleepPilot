% X ist die Matrix mit den Daten
% T ist die Matrix mit den gescorten Epochen
% T enthält die Hypnogramme und besteht aus 5 Spalten
% (1: W, 2: N1, 3: N2, 4: N3, 5: REM). Die "1" markiert das gescorte
% Stadium. X enthält die Daten; jede Spalte ist eine Epoche.

%fid = fopen('magic5.txt', 'w');
%fwrite(fid, M, 'float32');
%fclose(fid);

function F = createEEGTrainingData(X,T)

dimX = size(X);
zeilenX = dimX(1);
spaltenX = dimX(2);

F = zeros((zeilenX+1),spaltenX);

dimT = size(T);
zeilenT = dimT(1);
spaltenT = dimT(2);

for i=1:zeilenT
    for y=1:spaltenT
        if T(i,y) == 1
            F(1,i) = y;
        end
    end
end

for i=1:spaltenX
    for y=1:zeilenX
        F((y+1),i) = X(y,i);
    end
end
