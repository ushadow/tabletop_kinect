function mat = data2mat(data)
% DATA2MAT combines vectors in each cell into columns of a matrix.
nfeature = length(data{1}{1});
for i = 1 : length(data)
  data{i} = cell2mat(data{i});
end
mat = cell2mat(data);
assert(size(mat, 1) == nfeature);
end