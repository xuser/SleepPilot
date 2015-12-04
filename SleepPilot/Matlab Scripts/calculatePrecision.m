function F = calculatePrecision(T, erg, from, to)

size = to - from;
size = size + 1;

F = zeros(size,2);

for i=from:to
    for y=1:5
        if T(i,y) == 1
            F((i-from+1),1) = y;
        end
    end
end

F(:,2) = erg;

correct = 0;

for(z=1:size)
    if F(z,1) == F(z,2)
        correct = correct + 1;
    end
end

% Prints the precision
correct/size


