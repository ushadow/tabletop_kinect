function standardized = standardizefeature(data, retMat)
% Args
% - data: cell array
%
% Return
% - standardized: if reMat is true, returns a matrix, otherwise returns a 
%                 cell array.

mat = data2mat(data); % Each column is a feature vector.
standardized = standardize(mat);
if ~retMat
  ndx = 0;
  for i = 1 : length(data)
    for j = 1 : length(data{i})
      ndx = ndx + 1;
      data{i}{j} = mat(:, ndx);
    end
  end
  standardized = data;
end
end