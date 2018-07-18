# BenchFace

O BenchFace é um aplicativo de detecção de faces que usa Haar features baseado em classificadores em cascata,
um método proposto pelos pesquisadores [Viola and Jones 2001](https://ieeexplore.ieee.org/document/990517/).
O algoritmo de detecção de faces usa uma abordagem baseada em aprendizagem de máquina, onde funções em
cascata são treinadas com um conjunto de imagens positivas (imagens que contêm faces) e negativas (imagens
que não possuem faces). O aplicativo é composto de uma única imagem com 78 faces em diferentes ângulos.
O usuário pode alterar a mesma imagem para diferentes resoluções e algoritmos classificadores em cascata.
Sua execução pode ocorrer na nuvem, local e dinamicamente. Ao final da execução é exibida na tela a quantidade
de faces detectadas e o tempo decorrido para detectá-las.
