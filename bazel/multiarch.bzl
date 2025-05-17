load("@rules_oci//oci:defs.bzl", "oci_image", "oci_image_index", "oci_load", "oci_push")

def multi_arch_image(name, entrypoint, base_images, docker_repo, tar):
    image_names = []

    for arch, base in base_images.items():
        image_name = "{}_image_{}".format(name, arch)
        load_name = "{}_docker_{}".format(name, arch)

        oci_image(
            name = image_name,
            base = base,
            entrypoint = entrypoint,
            tars = [tar],
        )

        oci_load(
            name = load_name,
            image = ":{}".format(image_name),
            repo_tags = ["{}:latest".format(name)],
        )

        image_names.append(":{}".format(image_name))

    oci_image_index(
        name = "{}_image_index".format(name),
        images = image_names,
    )

    # NOTE: would need to be changed if pushing to registry other than docker hub
    oci_push(
        name = "{}_image_push".format(name),
        image = ":{}_image_index".format(name),
        remote_tags = ["latest"],
        repository = "index.docker.io/{}".format(docker_repo),
    )
