function y = sample_node(CPD, pev)

y = cell(1, 2);

if isempty(CPD.dps)
  i = 1;
else
  dpvals = cat(1, pev{CPD.dps});
  i = subv2ind(CPD.sizes(CPD.dps), dpvals(:)');
end

y{1} = gsamp(CPD.mean(:, i), CPD.cov(:, :, i), 1);
y{1} = y{1}(:);
y{2} = CPD.hand(:, :, i);