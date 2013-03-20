function [data modelparams] = preptrainingdata(dirname, modelparams)
files = dir(dirname);
for i = 1 : length(files)
  file = files(i);
  if ~file.isdir
    name = file.name;
    indices = strfind(name, '.');
    last_index = indices(1);
    basename = name(1 : last_index - 1);
    ext = name(last_index + 1 : end);
    if strcmp(ext, 'glab.csv')
      label = importdata([dirname name], ',', 1);
      feature = importdata([dirname basename '.gfet'], ',', 1);
      header = textscan(feature.textdata{1}, '%s%s%d%s%d', ...
                        'delimiter', ',');
      modelparams.nX = header{3};
      imageWidth = header{5};
      modelparams.handSize = imageWidth * imageWidth; 
      data = combinelabelfeature(label.data, feature.data, modelparams);
    end
  end
end
end

function data = combinelabelfeature(label, feature, modelparams)
label_frameid = label(:, 1);
feature_frameid = feature(:, 1);
[frameids, ilabel, ifeature] = intersect(label_frameid, feature_frameid);
fprintf('number of frames = %d', length(frameids));
segments = segment(frameids); % cell array
label = label(ilabel, :);

assert(modelparams.nG == length(unique(label(:, 2))));
assert(modelparams.nF == length(unique(label(:, 3))));
assert(check_label(label), 'Label is not valid.');

feature = feature(ifeature, :);
assert(size(label, 1) == size(feature, 1));
data = cell(1, length(segments));
for i = 1 : length(data)
  indices = segments{i};
  T = length(indices);
  data{i} = cell(modelparams.ss, T);
  data{i}(1 : 2, :) = num2cell(label(indices, 2 : 3)');
  feature_seg = feature(indices, 2 : end)';
  feature_cell = mat2cell(feature_seg, ...
      [modelparams.nX modelparams.handSize], ones(1, T));
  for t = 1 : T
    data{i}{4, t} = feature_cell(:, t);
  end
  assert(size(data{i}{1, 1}) == 1);
  assert(size(data{i}{2, 1}) == 1);
  assert(isempty(data{i}{3, 1}));
  assert(size(data{i}{4, 1}{1}) == modelparams.nX);
  assert(size(data{i}{4, 1}{2}) == modelparams.handSize);
end
end

function segments = segment(frameids)
% segments = segment(frameids) returns a cell array of indices vectors.
segments = {};
start = 1;
for i = 2 : length(frameids)
  if frameids(i) - 1 ~= frameids(i - 1)
    segments{end + 1} = start : i - 1; %#ok<AGROW>
    start = i;
  end
end
end

function valid = check_label(label)
% Checks the validity of G, F labeling.
nrows = size(label, 1);
for i = 1 : nrows - 1
  if label(i, 2) ~= label(i + 1, 2)
    valid = label(i, 3) == 2;
    if ~valid
      disp(label(i, :));
      disp(label(i + 1, :));
      return;
    end
  end
end
end