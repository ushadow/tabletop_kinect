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
  pie <- list()
  edges <- c()
  palette <- heat.colors(kNumDepthDivs)
  colors <- c()
  for (s in 0 : (kNumSectors - 1)) {
    extends <- c()
    for (r in 0 : (kNumRadDivs - 1)) {
      index <- r * kNumRadDivs + s + 1
      if (all(m[index,] == 0)) {
        next
      } else {
        index <- max.col(m[index,])
        colors <- c(colors, palette[index])
        extends <- c(extends, r) 
      }
    } 
    print(extends)
    pie <- c(pie, list(extends))
  }
  print(pie)
  radial.pie(pie, sector.colors = colors, start = -pi)
}
