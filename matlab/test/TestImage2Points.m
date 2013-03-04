classdef TestImage2Points < TestCase

methods
  function self = TestImage2Points(name)
    self = self@TestCase(name);
  end
  
  function test(self) %#ok<MANU>
    image = [1 2
             3 4];
    points = image2points(image);
    expected = [1 1 1
                2 1 3
                1 2 2
                2 2 4];
    assertTrue(all(points(:) == expected(:)));
  end
end
end
    