# BenchFace

O BenchFace é um aplicativo de detecção de faces que usa Haar features baseado em classificadores em cascata,
um método proposto pelos pesquisadores [Viola and Jones 2001](https://ieeexplore.ieee.org/document/990517/).
O algoritmo de detecção de faces usa uma abordagem baseada em aprendizagem de máquina, onde funções em
cascata são treinadas com um conjunto de imagens positivas (imagens que contêm faces) e negativas (imagens
que não possuem faces). O aplicativo é composto de uma única imagem com 78 faces em diferentes ângulos.
O usuário pode alterar a mesma imagem para diferentes resoluções e algoritmos classificadores em cascata.
Sua execução pode ocorrer na nuvem, local e dinamicamente. Ao final da execução é exibida na tela a quantidade
de faces detectadas e o tempo decorrido para detectá-las.


O BenchFace utiliza de alguns modulos como:

* Multiplatform Offloading System (MpOS). Diante de diversos desafios de mobile cloud computing, o framework [MpOS](http://mpos.great.ufc.br/?page_id=21) surgiu para auxiliar desenvolvedores de aplicações mobile resolverem problemas de offloading para multiplas plataformas. MpOS foi desenvolvido para suportar operações de offload em Android e Windows Phone, e sua arquitetura e requisitos foram inspirados em features de diferentes frameworks de offloading como: ThinkAir, MAUI, Cuckoo and Scavenger.

* Open Source Computer Vision Library (OpenCV). A [OpenCv](https://opencv.org/) originalmente, desenvolvida pela Intel, em 2000, é uma biblioteca multiplataforma, totalmente livre ao uso acadêmico e comercial, para o desenvolvimento de aplicativos na área de Visão computacional, bastando seguir o modelo de licença BSD Intel.
