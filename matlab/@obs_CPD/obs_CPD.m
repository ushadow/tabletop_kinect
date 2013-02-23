function CPD = obs_CPD(bnet, self, varargin)

CPD = init_fields;
CPD = class(CPD, 'custom_gaussian_CPD', generic_CPD(0));

ns = bnet.node_sizes;
ps = parents(bnet.dag, self);

end

function CPD = init_fields
CPD.self = [];
end