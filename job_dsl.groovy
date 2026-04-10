
// --- Folder Settings ---
final String folderName = 'Tools'
final String folderDescription = 'Folder for miscellaneous tools.'

// --- Log Rotation Settings ---
final Integer maxBuildsToKeep = 10
final Integer maxArtifactsToKeep = 2

// --- Parameter Descriptions ---
final String githubNameDescription = 'GitHub repository owner/repo_name (e.g.: "EpitechIT31000/chocolatine")'
final String displayNameDescription = 'Display name for the job'

// --- Job Template ---
/* groovylint-disable-next-line GStringExpressionWithinString */
final String jobTemplate = '''
    freeStyleJob(DISPLAY_NAME) {
        properties {
            githubProjectUrl("https://github.com/${GITHUB_NAME}")
        }
        scm {
            git {
                remote { url("https://github.com/${GITHUB_NAME}.git") }
                branches('master', 'main')
            }
        }
        triggers {
            scm('* * * * *')
        }
        wrappers {
            preBuildCleanup()
        }
        steps {
            shell('make fclean')
            shell('make')
            shell('make tests_run')
            shell('make clean')
        }
    }
'''.stripIndent()

final String privateJobTemplate = '''
    freeStyleJob(DISPLAY_NAME) {
        properties {
            githubProjectUrl("https://github.com/${GITHUB_NAME}")
        }
        scm {
            git {
                remote { 
                    url("https://github.com/${GITHUB_NAME}.git")
                    credentials('github-auth')
                }
                branches('master', 'main')
            }
        }
        triggers {
            scm('* * * * *')
        }
        wrappers {
            preBuildCleanup()
        }
        steps {
            shell('if [ -f Makefile ]; then make fclean; make; make tests_run; make clean; elif [ -f CMakeLists.txt ]; then cmake -B build && cmake --build build && ctest --test-dir build; else echo "No build files found" && exit 1; fi')
        }
    }
'''.stripIndent()

folder(folderName) {
    description(folderDescription)
}

freeStyleJob("${folderName}/clone-repository") {
    description('Job to clone a Git repository using a provided URL.')

    parameters {
        stringParam('GIT_REPOSITORY_URL', '', 'Git URL of the repository to clone')
    }

    wrappers {
        preBuildCleanup()
    }

    steps {
        shell('git clone $GIT_REPOSITORY_URL')
    }

    logRotator {
        numToKeep(maxBuildsToKeep)
        artifactNumToKeep(maxArtifactsToKeep)
    }
}

freeStyleJob("${folderName}/SEED") {
    description('Seed job to create new Jenkins jobs based on a GitHub repository.')

    parameters {
        stringParam('GITHUB_NAME', '', githubNameDescription)
        stringParam('DISPLAY_NAME', '', displayNameDescription)
    }

    steps {
        dsl {
            text(jobTemplate)
        }
    }

    logRotator {
        numToKeep(maxBuildsToKeep)
        artifactNumToKeep(maxArtifactsToKeep)
    }
}

freeStyleJob("${folderName}/clone-private-repository") {
    description('Job to clone a private Git repository using a provided URL and credentials.')

    parameters {
        stringParam('GIT_REPOSITORY_URL', '', 'HTTPS URL of the private repository (https://github.com/user/repo.git)')
    }

    wrappers {
        preBuildCleanup()
    }

    scm {
        git {
            remote {
                url('$GIT_REPOSITORY_URL')
                credentials('github-auth')
            }
            branches('*/master', '*/main')
        }
    }

    logRotator {
        numToKeep(maxBuildsToKeep)
        artifactNumToKeep(maxArtifactsToKeep)
    }
}

freeStyleJob("${folderName}/SEED-PRIVATE") {
    description('Seed job to create new Jenkins jobs based on a private GitHub repository using credentials.')

    parameters {
        stringParam('GITHUB_NAME', '', githubNameDescription)
        stringParam('DISPLAY_NAME', '', displayNameDescription)
    }

    steps {
        dsl {
            text(privateJobTemplate)
        }
    }

    logRotator {
        numToKeep(maxBuildsToKeep)
        artifactNumToKeep(maxArtifactsToKeep)
    }
}
