job "critter-bot" {
    type = "service"

    region      = "us"
    datacenters = ["prod"]

    all_at_once = false

    constraint {
      attribute = "${attr.consul.datacenter}"
      operator  = "="
      value     = "prod"
    }

    constraint {
      attribute = "${meta.application_server}"
      operator  = "="
      value     = "True"
    }

    group "critter-bot" {
        count = 1

        constraint {
          operator  = "distinct_hosts"
          value     = "true"
        }

        task "critter-bot" {
            driver = "docker"

            config {
                image = "278696104475.dkr.ecr.us-east-1.amazonaws.com/critter-bot:[[ .version ]]"
                force_pull = true

                port_map {
                    http = 9600
                }
            }
            
            template {
                destination = "secrets/.env"
                env         = true
                data        = <<EOH
DEPLOY_TIME="[[ timeNowUTC ]]"

SLACK_TOKEN={{ key "critter-bot" "slack-token" }}
TWITTER_ENCODED_CREDS={{ key "critter-bot" "twitter-creds" }}
TWITTER_ACCOUNTS={{ keyOrDefault "critter-bot/accounts" "RaccoonEveryHr,PossumEveryHour,EveryBat,EverythingGoats,CUTEFUNNYANIMAL,quokkaeveryhour,hourlyFox,RaccoonDaily" }}
SLACK_CHANNEL=trash-critters
                EOH
            }

            service {
              name = "critter-bot"
              tags = ["fun"]
            }

            vault {
                policies    = ["default", "prod"]
                change_mode = "restart"
            }

            resources {
              cpu    = 50
              memory = 300

              network {
                mbits = 1
              }
            }
        }
    }

    meta {
        name       = "critter-bot"
        namespace  = "prod"
        stack_name = "critter-bot"
        unit_name  = "critter-bot"
        nelson     = false
    }
}