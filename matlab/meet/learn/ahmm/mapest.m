function mapEst = mapest(engine, hnode, T)
% Args:
% - hnode: hidden node that we want to estimate the state.
% Return
% - mapEst: a cell array.
nhnode = length(hnode);
mapEst = cell(nhnode, T);
for t = 1 : T
  for i = 1 : nhnode
    m = marginal_nodes(engine, hnode(i), t);
    [~, ndx] = max(m.T);
    mapEst{i, t} = ndx;
  end
end