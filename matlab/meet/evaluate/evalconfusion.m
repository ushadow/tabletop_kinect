function cm = evalconfusion(varargin)

cm = zeros(6, 6);
for i = 1 : 2 : length(varargin)
  Y = varargin{i}.Y;
  R = varargin{i + 1};
  nfold = size(R, 2);
  for j = 1 : nfold
    split = R{j}.split;
    Ytrue = Y(split{2});
    cm = evalOneFold(Ytrue, R{j}.prediction.validate, cm);
  end
end
total = sum(cm, 2);
total = repmat(total(:), 1, size(cm, 2));
cm = round(cm * 100 ./ total);
end

function cm = evalOneFold(Ytrue, Ystar, cm)
% - Ytrue: cell arrary of sequences.
for i = 1 : length(Ytrue)
  for j = 1 : size(Ytrue{i}, 2)
    cm(Ytrue{i}{1, j}, Ystar{i}{1, j}) = cm(Ytrue{i}{1, j}, Ystar{i}{1, j}) + 1; 
  end
end
end