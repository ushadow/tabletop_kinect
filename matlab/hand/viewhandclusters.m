function viewhandclusters(data, classes, prefix)
nclass = unique(classes);
for c = nclass(:)'
  disp(c);
  data1 = data(:, classes == c);
  H = viewhand(data1);
  saveas(H, [prefix 'class' int2str(c) '.jpeg'], 'jpeg');
  close(H);
end
end