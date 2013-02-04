library(mclust)
library(plotrix)

# Args:
# input.file: string of input file name. The data should be comma delimited and
#     the first column is the frame ID.
Cluster <- function(input.file, output.file) {
  data <- read.table(input.file, sep = ',')
  numcol <- ncol(data)
  descriptor.mclust <- Mclust(data[, 2 : numcol], G = 1:2)
  output <- data.frame(data[, 1], descriptor.mclust$classification)
  write.table(output, output.file, sep = ',', row.names = FALSE, col.names =
              FALSE)
}

PlotDescriptor <- function(descriptor) {
  kNumDepthDivs <- 5
  kNumSectors <- 8
  kNumRadDivs <- 5
  m <- matrix(descriptor, kNumSectors * kNumRadDivs, kNumDepthDivs, TRUE)
  palette <- heat.colors(kNumDepthDivs)
  for (r in (kNumRadDivs - 1) : 0){
    edges <- c()
    colors <- c()
    extends <- list()
    for (s in 0 : (kNumSectors - 1)) {
      # Row index. 
      rindex <- r * kNumRadDivs + s + 1
      if (all(m[rindex,] == 0)) { colors <- c(colors, "#FFFFFFFF")
      } else {
        # Depth index.
        dindex <- which.max(m[rindex,])
        colors <- c(colors, palette[dindex])
      }
      extends <- c(extends, list(r : (r + 1))) 
      edges <- c(edges, -pi + pi * 2 * s / kNumSectors) 
    } 
    edges <- c(edges, pi)
    add = TRUE 
    if (names(dev.cur()) == "null device") {
      add = FALSE
    }
    print(extends)
    print(colors)
    print(edges)
    print(add)
    radial.pie(extends, sector.colors = colors, start = -pi, 
               show.grid.labels = FALSE, add = add)
  }
}
