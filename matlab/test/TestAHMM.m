classdef TestAHMM < TestCase
  
properties
  ahmm
end

methods
  function self = TestAHMM(name)
    self = self@TestCase(name);
  end
    
  function setUp(self)
    params.nG = 4;
    params.nS = 4;
    params.nF = 2;
    params.nX = 2;

    params.Gstartprob = [1 0 0 0];
    params.Gtransprob = [0 0.45 0.45 0.1
                         0 0.1 0.1 0.8
                         0 0.1 0.1 0.8
                         1 0 0 0];

    % 2 x 4 matrix                   
    params.Sstartprob = [1 0 0 0
                         0 1 0 0
                         0 0 1 0
                         0 0 0 1];
                       
    params.Stransprob = zeros(params.nS, params.nG, params.nS);
    params.Stransprob(:, 1, :) = [1 0 0 0 
                                  1 0 0 0 
                                  1 0 0 0 
                                  1 0 0 0];
    params.Stransprob(:, 2, :) = [0 1 0 0 
                                  0 1 0 0
                                  0 1 0 0
                                  0 1 0 0];

    params.Stransprob(:, 3, :) = [0 0 1 0 
                                  0 0 1 0
                                  0 0 1 0
                                  0 0 1 0];
                                
    params.Stransprob(:, 4, :) = [0 0 0 1 
                                  0 0 0 1
                                  0 0 0 1
                                  0 0 0 1];

    params.Stermprob = ones(params.nG, params.nS, 2);
    params.Stermprob(1, :, :) = [0 1
                                 0 1
                                 0 1
                                 0 1];
    params.Stermprob(2, :, :) = [0 1
                                 0 1
                                 0 1
                                 0 1];
    params.Stermprob(3, :, :) = [0 1
                                 0 1
                                 0 1
                                 0 1];
    params.Stermprob(4, :, :) = [0 1
                                 0 1
                                 0 1
                                 0 1];
                             
    n = 4;
    params.hand = zeros(n, n, params.nS);
    for i = 1 : params.nS
      params.hand(:, :, i) = repmat(i, n);
    end

    params.hd_mu = 0;
    params.hd_sigma = 1;

    params.Xmean = reshape(1 : 2 * n, [params.nX params.nS]);
    params.Xcov = repmat(eye(params.nX) * 0.1, [1, 1, params.nS]);

    self.ahmm = createmodel(params);
  end

  function testSample(self)
    T = 10;
    evidence = sample_dbn(self.ahmm, T);

    G1 = 1; S1 = 2; F1 = 3; X1 = 4; 
    for i = 1 : T
      s = evidence{S1, i};
      hand = evidence{X1, i}{2};
      assertTrue(all(hand(:) == s));
      assertTrue(s == evidence{G1, i});
      if i > 1 
        f = evidence{F1, i - 1};
        if f == 1
          assertTrue(evidence{G1, i - 1} == evidence{G1, i})
        else
          assertTrue(f == 2);
        end
      end
    end
  end
  
  function testInference(self)
    engine = smoother_engine(jtree_2TBN_inf_engine(self.ahmm));

    T = 4;
    ss = length(self.ahmm.intra);
    assertTrue(ss == 4);
    onodes = self.ahmm.observed;
    assertTrue(onodes == 4);

    maxIter = 10;
    for i = 1 : maxIter
      ev = sample_dbn(self.ahmm, 'length', T);
      %logdebug('AHMM', 'ev', ev);
      evidence = cell(ss, T);
      evidence(onodes, :) = ev(onodes, :);

      engine = enter_evidence(engine, evidence);
      hnodes = mysetdiff(1 : ss, onodes);
      map = TestAHMM.get_marginals(engine, hnodes, T);
      %logdebug('AHMM', 'map', map);
      for t = 1 : T
        for n = hnodes(:)'
          assertTrue(map(n, t) == ev{n, t});
        end
      end
    end
  end
end

methods(Static)
  function map = get_marginals(engine, hnodes, T)
    map = zeros(length(hnodes), T);
    for t = 1 : T
      for n = hnodes(:)'
        m = marginal_nodes(engine, n, t);
        [~, index] = max(m.T);
        map(n, t) = index;
      end
    end
  end
end
 
end
